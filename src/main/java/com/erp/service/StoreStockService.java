package com.erp.service;

import com.erp.repository.StoreStockRepository;
import com.erp.repository.entity.StoreStock;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class StoreStockService {

    private final StoreStockRepository storeStockRepository;

    private int getLatestQuantity(Long storeItemNo) {
        var log = storeStockRepository.findFirstByStoreItemNoOrderByStoreStockNoDesc(storeItemNo);
        return (log == null) ? 0 : log.getCurrentQuantity();
    }

    @Transactional
    public int dispose(Long storeItemNo, int quantity, String reason) {
        if (quantity <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "폐기 수량은 1 이상이어야 합니다.");
        }
        int latest = getLatestQuantity(storeItemNo);
        if (quantity > latest) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "폐기 수량이 현재 재고를 초과합니다.");
        }

        var saved = storeStockRepository.save(
                StoreStock.builder()
                        .storeItemNo(storeItemNo)
                        .changeQuantity(-quantity)          // 마이너스
                        .changeReason("폐기")
                        .currentQuantity(latest - quantity) // 최신 재고 반영
                        .disposalReason(reason)
                        .build()
        );
        return saved.getCurrentQuantity();
    }
}
