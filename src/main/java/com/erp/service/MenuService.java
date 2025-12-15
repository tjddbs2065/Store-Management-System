package com.erp.service;

import com.erp.awss3.S3Uploader;
import com.erp.controller.exception.NoMenuException;
import com.erp.dao.ItemDAO;
import com.erp.dao.MenuDAO;
import com.erp.dao.StoreDAO;
import com.erp.dto.ItemDTO;
import com.erp.dto.MenuDTO;
import com.erp.dto.MenuIngredientDTO;
import com.erp.dto.StoreDTO;
import com.erp.repository.MenuIngredientRepository;
import com.erp.repository.StoreMenuRepository;
import com.erp.repository.entity.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MenuService {
    private final MenuDAO menuDAO;
    private final StoreDAO storeDAO;
    private final ItemDAO itemDAO;
    private final MenuIngredientRepository menuIngredientRepository;
    private final StoreMenuRepository storeMenuRepository;
    private final S3Uploader s3Uploader;

    //전체 메뉴 조회
    public List<MenuDTO> getMenuList(String menuCategory, String releaseStatus) {
        return menuDAO.getMenuList(menuCategory, releaseStatus);
    }

    //상세조회
    @Transactional
    public MenuDTO getMenuDetail(Long menuNo) {

        // 1) 단일 메뉴 조회
        MenuDTO menu = menuDAO.getMenuByMenuNo(menuNo);
        if (menu == null) throw new NoMenuException("없는 메뉴입니다.");

        // 2) 코드로 전체 사이즈 메뉴 조회 (L/M/단일)
        List<MenuDTO> sizeList = menuDAO.getMenuByMenuCode(menu.getMenuCode());
        boolean hasSize = sizeList.size() > 1;

        // 3) 재료를 사이즈별로 정리하여 하나의 리스트로 병합
        List<MenuIngredientDTO> mergedIngredients = getIngredientDTOList(sizeList);

        // 4) DTO에 담기
        menu.setSizeList(sizeList);
        menu.setIngredients(mergedIngredients);
        menu.setHasSize(hasSize);

        return menu;
    }


    // ⭐ 최종 — L/M/단일 사이즈 전체를 받아서 재료를 합치는 메서드
    private List<MenuIngredientDTO> getIngredientDTOList(List<MenuDTO> sizeList) {

        Map<Long, MenuIngredientDTO> merged = new HashMap<>();

        for (MenuDTO m : sizeList) {
            String size = m.getSize();
            List<MenuIngredientDTO> ingList =
                    menuIngredientRepository.findByMenu_MenuNo(m.getMenuNo())
                            .stream()
                            .map(i -> {
                                MenuIngredientDTO dto = new MenuIngredientDTO();
                                dto.setItemNo(i.getItem().getItemNo());
                                dto.setMenuNo(m.getMenuNo());
                                dto.setIngredientName(i.getItem().getIngredientName());
                                dto.setStockUnit(i.getItem().getStockUnit());

                                if ("라지".equals(size)) dto.setQuantityLarge(i.getIngredientQuantity());
                                else if ("미디움".equals(size)) dto.setQuantityMedium(i.getIngredientQuantity());
                                else dto.setQuantity(i.getIngredientQuantity());

                                return dto;
                            })
                            .toList();

            for (MenuIngredientDTO ing : ingList) {

                Long itemNo = ing.getItemNo();
                MenuIngredientDTO base = merged.getOrDefault(itemNo, new MenuIngredientDTO());

                base.setItemNo(itemNo);
                base.setIngredientName(ing.getIngredientName());
                base.setStockUnit(ing.getStockUnit());

                if (ing.getQuantityLarge() != null)
                    base.setQuantityLarge(ing.getQuantityLarge());

                if (ing.getQuantityMedium() != null)
                    base.setQuantityMedium(ing.getQuantityMedium());

                if (ing.getQuantity() != null)
                    base.setQuantity(ing.getQuantity());

                merged.put(itemNo, base);
            }
        }
        return new ArrayList<>(merged.values());
    }

    public void addMenu(MenuDTO menuRequest, MultipartFile menuImage) {
        if (menuImage != null && !menuImage.isEmpty()) {
            String imageUrl = s3Uploader.uploadMenuImage(menuImage, menuRequest.getMenuCode());
            menuRequest.setMenuImage(imageUrl);
        }

        if ("Y".equals(menuRequest.getSize())) {
            MenuDTO largeDTO = MenuDTO.builder()
                    .menuName(menuRequest.getMenuName())
                    .menuCode(menuRequest.getMenuCode())
                    .menuCategory(menuRequest.getMenuCategory())
                    .menuPrice(menuRequest.getMenuPriceLarge())
                    .menuExplain(menuRequest.getMenuExplain())
                    .size("라지")
                    .menuImage(menuRequest.getMenuImage())
                    .releaseStatus(menuRequest.getReleaseStatus())
                    .build();
            menuDAO.addMenu(largeDTO);
            Long largeMenuNo = largeDTO.getMenuNo();
            if (largeMenuNo == null) {
                throw new RuntimeException("fail");
            }

            for (MenuIngredientDTO ing : menuRequest.getIngredients()) {
                if (ing.getQuantityLarge() == null) continue;

                ItemDTO itemDTO = itemDAO.getItemByItemNo(ing.getItemNo());

                if (itemDTO == null) {
                    throw new RuntimeException("Item not found: " + ing.getItemNo());
                }

                Item item = Item.builder()
                        .itemNo(itemDTO.getItemNo())
                        .build();

                MenuIngredient entity = MenuIngredient.builder()
                        .menu(Menu.builder().menuNo(largeMenuNo).build())
                        .item(item) // 여기 중요
                        .ingredientQuantity(ing.getQuantityLarge())
                        .build();

                menuIngredientRepository.save(entity);
            }



            MenuDTO mediumDTO = MenuDTO.builder()
                    .menuName(menuRequest.getMenuName())
                    .menuCode(menuRequest.getMenuCode())
                    .menuCategory(menuRequest.getMenuCategory())
                    .menuPrice(menuRequest.getMenuPriceMedium())
                    .menuExplain(menuRequest.getMenuExplain())
                    .size("미디움")
                    .menuImage(menuRequest.getMenuImage())
                    .releaseStatus(menuRequest.getReleaseStatus())
                    .build();
            menuDAO.addMenu(mediumDTO);
            Long mediumMenuNo = mediumDTO.getMenuNo();
            if (mediumMenuNo == null) {
                throw new RuntimeException("fail");
            }

            for (MenuIngredientDTO ing : menuRequest.getIngredients()) {

                if (ing.getQuantityMedium() == null) continue;

                ItemDTO itemDTO = itemDAO.getItemByItemNo(ing.getItemNo());
                if (itemDTO == null) throw new RuntimeException("Item not found: " + ing.getItemNo());

                MenuIngredient entity = MenuIngredient.builder()
                        .menu(Menu.builder().menuNo(mediumMenuNo).build())
                        .item(Item.builder().itemNo(itemDTO.getItemNo()).build())
                        .ingredientQuantity(ing.getQuantityMedium())
                        .build();

                menuIngredientRepository.save(entity);
            }

            if ("출시 중".equals(menuRequest.getReleaseStatus())) {
                addStoreMenu(largeMenuNo);
                addStoreMenu(mediumMenuNo);
            }

        } else {
            MenuDTO oneDTO = MenuDTO.builder()
                    .menuName(menuRequest.getMenuName())
                    .menuCode(menuRequest.getMenuCode())
                    .menuCategory(menuRequest.getMenuCategory())
                    .menuPrice(menuRequest.getMenuPrice())
                    .menuExplain(menuRequest.getMenuExplain())
                    .size("단일")
                    .menuImage(menuRequest.getMenuImage())
                    .releaseStatus(menuRequest.getReleaseStatus())
                    .build();
            menuDAO.addMenu(oneDTO);
            Long menuNo = oneDTO.getMenuNo();

            if (menuNo == null) {
                throw new RuntimeException("fail");
            }

            for (MenuIngredientDTO ing : menuRequest.getIngredients()) {

                ItemDTO itemDTO = itemDAO.getItemByItemNo(ing.getItemNo());
                if (itemDTO == null) throw new RuntimeException("Item not found: " + ing.getItemNo());

                MenuIngredient entity = MenuIngredient.builder()
                        .menu(Menu.builder().menuNo(menuNo).build())
                        .item(Item.builder().itemNo(itemDTO.getItemNo()).build())
                        .ingredientQuantity(ing.getQuantity())
                        .build();

                menuIngredientRepository.save(entity);
            }

            if ("출시 중".equals(menuRequest.getReleaseStatus())) {
                addStoreMenu(menuNo);
            }
        }
    }

    private void addStoreMenu(Long menuNo){
        List<StoreDTO> stores = storeDAO.getActiveStores();

        for (StoreDTO store : stores) {
            StoreMenu storeMenu = StoreMenu.builder()
                    .store(Store.builder().storeNo(store.getStoreNo()).build())
                    .menu(Menu.builder().menuNo(menuNo).build())
                    .salesStatus("판매중단")
                    .build();
            storeMenuRepository.save(storeMenu);
        }
    }

    @Transactional
    public void updateMenu(MenuDTO menuRequest, MultipartFile menuImage) {

        if (menuRequest.isRemoveImage()) {
            menuRequest.setMenuImage(null);

        } else if (menuImage != null && !menuImage.isEmpty()) {
            String imageUrl =
                    s3Uploader.uploadMenuImage(menuImage, menuRequest.getMenuCode());
            menuRequest.setMenuImage(imageUrl);

        } else {
            menuRequest.setMenuImage(menuRequest.getOriginImage());
        }

        List<MenuDTO> oldList = menuDAO.getMenuByMenuCode(menuRequest.getMenuCode());
        if (oldList == null || oldList.isEmpty()) {
            throw new RuntimeException("메뉴 정보를 찾을 수 없습니다.");
        }

        // 기존 상태
        String oldStatus = oldList.get(0).getReleaseStatus();
        // 새 상태
        String newStatus = menuRequest.getReleaseStatus();

        boolean hasSize = "Y".equals(menuRequest.getSize());

        for (MenuDTO old : oldList) {

            Integer menuPrice = null;

            if (hasSize) {
                if (old.getSize().equals("라지")) {
                    menuPrice = menuRequest.getMenuPriceLarge();
                } else if (old.getSize().equals("미디움")) {
                    menuPrice = menuRequest.getMenuPriceMedium();
                }
            } else {
                menuPrice = menuRequest.getMenuPrice();
            }

            MenuDTO updateDTO = MenuDTO.builder()
                    .menuNo(old.getMenuNo())
                    .menuCode(menuRequest.getMenuCode())
                    .menuName(menuRequest.getMenuName())
                    .menuCategory(old.getMenuCategory())
                    .menuExplain(menuRequest.getMenuExplain())
                    .menuImage(menuRequest.getMenuImage())
                    .menuPrice(menuPrice)
                    .releaseStatus(newStatus)
                    .build();

            menuDAO.setMenu(updateDTO);
        }

        for (MenuDTO old : oldList) {
            menuIngredientRepository.deleteByMenu_MenuNo(old.getMenuNo());
        }

        for (MenuIngredientDTO ing : menuRequest.getIngredients()) {

            if (!hasSize) {
                Integer qty = ing.getQuantity();
                if (qty == null) continue;

                Long menuNo = oldList.get(0).getMenuNo();

                MenuIngredient entity = MenuIngredient.builder()
                        .menu(Menu.builder().menuNo(menuNo).build())
                        .item(Item.builder().itemNo(ing.getItemNo()).build())
                        .ingredientQuantity(qty)
                        .build();

                menuIngredientRepository.save(entity);
                continue;
            }

            for (MenuDTO m : oldList) {

                Integer qty = null;

                if (m.getSize().equals("라지")) {
                    qty = ing.getQuantityLarge();
                } else if (m.getSize().equals("미디움")) {
                    qty = ing.getQuantityMedium();
                }

                if (qty == null) continue;

                MenuIngredient entity = MenuIngredient.builder()
                        .menu(Menu.builder().menuNo(m.getMenuNo()).build())
                        .item(Item.builder().itemNo(ing.getItemNo()).build())
                        .ingredientQuantity(qty)
                        .build();

                menuIngredientRepository.save(entity);
            }
        }

        if (!"출시 중".equals(oldStatus) && "출시 중".equals(newStatus)) {

            List<StoreDTO> stores = storeDAO.getActiveStores();

            for (StoreDTO store : stores) {

                for (MenuDTO m : oldList) {

                    boolean exists =
                            storeMenuRepository.existsByStore_StoreNoAndMenu_MenuNo(
                                    store.getStoreNo(),
                                    m.getMenuNo()
                            );

                    if (!exists) {
                        StoreMenu storeMenu = StoreMenu.builder()
                                .store(Store.builder().storeNo(store.getStoreNo()).build())
                                .menu(Menu.builder().menuNo(m.getMenuNo()).build())
                                .salesStatus("판매중단")
                                .build();

                        storeMenuRepository.save(storeMenu);
                    }
                }
            }
        }
    }


    @Transactional
    public void removeMenu(Long menuNo) {

        MenuDTO menu = menuDAO.getMenuByMenuNo(menuNo);
        if (menu == null) {
            throw new NoMenuException("없는 메뉴입니다.");
        }

        List<MenuDTO> sameCodeMenus = menuDAO.getMenuByMenuCode(menu.getMenuCode());

        for (MenuDTO m : sameCodeMenus) {
            Long targetMenuNo = m.getMenuNo();
            storeMenuRepository.deleteByMenu_MenuNo(targetMenuNo);
            menuIngredientRepository.deleteByMenu_MenuNo(targetMenuNo);
            menuDAO.removeMenu(targetMenuNo);
        }
    }
}
