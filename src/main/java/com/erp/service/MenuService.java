package com.erp.service;

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

    public void addMenu(MenuDTO menuRequest) {
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
}
