package com.erp.service;

import com.erp.dto.StoreDTO;
import com.erp.repository.StoreRepository;
import com.erp.repository.entity.Store;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StoreSearchModalService {

    private final StoreRepository storeRepository;

    /**
     * [모달용] 직영점 목록 조회
     * - keyword가 있으면 storeName LIKE 검색
     * - 없으면 전부 조회 (storeName ASC)
     */
    public List<StoreDTO> getStores(String keyword) {
        List<Store> stores = StringUtils.hasText(keyword)
                ? storeRepository.findByStoreNameContaining(keyword)
                : storeRepository.findAll(Sort.by(Sort.Direction.ASC, "storeName"));

        return stores.stream().map(this::toDTO).toList();
    }


    private StoreDTO toDTO(Store s) {
        return StoreDTO.builder()
                .storeNo(s.getStoreNo())
                .storeName(s.getStoreName())
                .storeStatus(s.getStoreStatus())
                .storeManagerId(s.getManager() != null ? s.getManager().getManagerId() : null)
                .managerName(s.getManager() != null ? s.getManager().getManagerName() : null)
                .email(s.getManager() != null ? s.getManager().getEmail() : null)
                .address(s.getAddress())
                .storePhoneNumber(s.getStorePhoneNumber())
                .openedDate(s.getOpenedDate() != null ? s.getOpenedDate().toString() : null)
                .closedDate(s.getClosedDate() != null ? s.getClosedDate().toString() : null)
                .openTime(s.getOpenTime())
                .closeTime(s.getCloseTime())
                .latitude(s.getLatitude())
                .longitude(s.getLongitude())
                .storeImage(s.getStoreImage())
                .menuStopRole(s.getMenuStopRole())
                .build();
    }
}
