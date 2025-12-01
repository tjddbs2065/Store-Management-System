package com.erp.service;

import com.erp.dto.SalesListDTO;
import com.erp.dto.StoreDailyMenuSalesDTO;
import com.erp.repository.StoreOrderDetailRepository;
import com.erp.repository.StoreSalesRepository;
import com.erp.repository.entity.StoreSales;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SalesListService {

    private final StoreSalesRepository storeSalesRepository;
    private final StoreOrderDetailRepository storeOrderDetailRepository;


    public List<StoreDailyMenuSalesDTO> getSalesDetail(Long storeNo, LocalDate salesDate){

        LocalDateTime start = salesDate.atStartOfDay();
        LocalDateTime end   = salesDate.plusDays(1).atStartOfDay();

        return storeOrderDetailRepository.findDailyMenuSalesByStore(storeNo, start, end);
    }


    public Page<SalesListDTO> getSalesList(String startDateStr,
                                           String endDateStr,
                                           String storeName,
                                           Pageable pageable) {

        LocalDate startDate = null;
        LocalDate endDate = null;

        if (startDateStr != null && !startDateStr.isBlank()) {
            startDate = LocalDate.parse(startDateStr);
        }
        if (endDateStr != null && !endDateStr.isBlank()) {
            endDate = LocalDate.parse(endDateStr);
        }

        Page<SalesListDTO> page =
                storeSalesRepository.findSalesList(startDate, endDate, storeName, pageable);

        List<SalesListDTO> content = page.getContent();
        applyGrowthRate(content);

        return new PageImpl<>(content, pageable, page.getTotalElements());
    }



    public Page<SalesListDTO> getSalesListForStore(Long storeNo,
                                                   String startDateStr,
                                                   String endDateStr,
                                                   Pageable pageable) {

        LocalDate startDate = null;
        LocalDate endDate = null;

        if (startDateStr != null && !startDateStr.isBlank()) {
            startDate = LocalDate.parse(startDateStr);
        }
        if (endDateStr != null && !endDateStr.isBlank()) {
            endDate = LocalDate.parse(endDateStr);
        }

        // ⭐ Repository도 storeNo 버전 필요
        Page<SalesListDTO> page =
                storeSalesRepository.findSalesListForStore(storeNo, startDate, endDate, pageable);

        List<SalesListDTO> content = page.getContent();
        applyGrowthRate(content);

        return new PageImpl<>(content, pageable, page.getTotalElements());
    }



    private void applyGrowthRate(List<SalesListDTO> content) {

        for (SalesListDTO dto : content) {

            LocalDate prevDay = dto.getSalesDate().minusDays(1);

            List<StoreSales> prevList =
                    storeSalesRepository.findByStore_StoreNoAndSalesDateBetween(
                            dto.getStoreNo(),
                            prevDay,
                            prevDay
                    );

            if (!prevList.isEmpty()) {
                int prevAmount = prevList.get(0).getSalesPrice();
                int todayAmount = dto.getSalesAmount();

                if (prevAmount > 0) {
                    double rate = ((double)(todayAmount - prevAmount) / prevAmount) * 100;
                    dto.setGrowthRate(String.format("%.1f%% %s",
                            Math.abs(rate),
                            rate >= 0 ? "상승" : "하락"));
                } else {
                    dto.setGrowthRate("-");
                }
            } else {
                dto.setGrowthRate("-");
            }
        }
    }

}