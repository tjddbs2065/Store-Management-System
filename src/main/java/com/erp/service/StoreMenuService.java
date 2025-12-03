package com.erp.service;

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
                    dto.getMenuCode(),
                    dto.getMenuName(),
                    new ArrayList<>()
            ));
            grouped.get(code).getItems().add(dto);
        }

        return new ArrayList<>(grouped.values());
    }


    public List<StoreMenuDTO> getStoreMenu(Long storeNo) {
        return storeMenuRepository.findStoreMenuByStoreNo(storeNo);
    }
}
