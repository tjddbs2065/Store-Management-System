package com.erp.service;

import com.erp.dto.MenuStatusDTO;
import com.erp.dto.StoreMenuDTO;
import com.erp.dto.StoreMenuGroupedDTO;
import com.erp.repository.StoreMenuRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StoreMenuService {

    private final StoreMenuRepository storeMenuRepository;


    public void updateStatus(List<MenuStatusDTO> updates) {
        for (MenuStatusDTO dto : updates) {
            storeMenuRepository.setSalesStatus(
                    dto.getStoreMenuNo(),
                    dto.getSalesStatus()
            );
        }
    }


    public List<StoreMenuGroupedDTO> getStoreMenuList(Long storeNo,
                                                      String menuName,
                                                      String salesStatus,
                                                      String menuCategory) {

        if (menuName != null && menuName.trim().isEmpty()) menuName = null;
        if (salesStatus != null && salesStatus.trim().isEmpty()) salesStatus = null;
        if (menuCategory != null && menuCategory.trim().isEmpty()) menuCategory = null;

        List<StoreMenuDTO> rawList =
                storeMenuRepository.findStoreMenuForStore(
                        storeNo,
                        menuName,
                        salesStatus,
                        menuCategory,
                        null
                ).getContent();

        Map<String, StoreMenuGroupedDTO> grouped = new LinkedHashMap<>();

        for (StoreMenuDTO dto : rawList) {

            String code = dto.getMenuCode();

            grouped.putIfAbsent(code, new StoreMenuGroupedDTO(
                    dto.getStoreName(),
                    dto.getMenuCode(),
                    dto.getMenuName(),
                    dto.getMenuCategory(),
                    new ArrayList<>()
            ));
            grouped.get(code).getItems().add(dto);
        }

        return new ArrayList<>(grouped.values());
    }


    public List<StoreMenuDTO> getStoreMenu(Long storeNo) {
        return storeMenuRepository.findStoreMenuByStoreNo(storeNo);
    }

    public List<StoreMenuGroupedDTO> searchMenu(
            String menuName,
            String salesStatus,
            String menuCategory
    ){
        if (salesStatus != null && salesStatus.trim().isEmpty()) salesStatus = null;
        if (menuCategory != null && menuCategory.trim().isEmpty()) menuCategory = null;

        List<StoreMenuDTO> rawList =
                storeMenuRepository.findStoreMenu(
                        null,
                        menuName,
                        salesStatus,
                        menuCategory,
                        null
                ).getContent();

        Map<String, StoreMenuGroupedDTO> grouped = new LinkedHashMap<>();

        for (StoreMenuDTO dto : rawList){
            String key = dto.getStoreName() + "-" + dto.getMenuCode();
            grouped.putIfAbsent(key, new StoreMenuGroupedDTO(
                    dto.getStoreName(),
                    dto.getMenuCode(),
                    dto.getMenuName(),
                    dto.getMenuCategory(),
                    new ArrayList<>()
            ));

            grouped.get(key).getItems().add(dto);
        }

        return new ArrayList<>(grouped.values());
    }

}
