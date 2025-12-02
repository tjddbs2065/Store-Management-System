package com.erp.service;

import com.erp.dao.StoreDAO;
import com.erp.dto.KPIDTO;
import com.erp.repository.SalesOrderRepository;
import com.erp.repository.StoreSalesRepository;
import com.erp.repository.entity.StoreSales;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.temporal.WeekFields;

@Service
@RequiredArgsConstructor
public class SalesKPIService {

    private final StoreSalesRepository storeSalesRepository;
    private final SalesOrderRepository salesOrderRepository;
    private final StoreDAO storeDAO;


    public KPIDTO getKPIByDate(String type, String start, String end, Long storeNo) {

        if (storeNo != null) {
            return getStoreKPI(storeNo, type, start, end);
        }
        return getOfficeKPI(type, start, end);
    }

    public KPIDTO getStoreKPI(Long storeNo, String type, String start, String end) {

        LocalDate startDate = convert(type, start, false);
        LocalDate endDate   = convert(type, end, true);

        int totalSales = sumStoreSales(storeNo, startDate, endDate);

        LocalDateTime startDt = startDate.atStartOfDay();
        LocalDateTime endDt   = endDate.atTime(23, 59, 59);

        Integer totalMenuCount = salesOrderRepository.getStoreMenuCount(storeNo, startDt, endDt);
        if (totalMenuCount == null) totalMenuCount = 0;

        int avgPrice = (totalMenuCount == 0) ? 0 : (totalSales / totalMenuCount);
        double growthRate = calcStoreWeeklyGrowth(storeNo);

        return KPIDTO.builder()
                .totalSales(totalSales)
                .totalMenuCount(totalMenuCount)
                .avgOrderAmount(avgPrice)
                .growthRate(growthRate)
                .build();
    }


    private KPIDTO getOfficeKPI(String type, String start, String end) {
        LocalDate startDate = convert(type, start, false);
        LocalDate endDate   = convert(type, end, true);
        return getOfficeKPI(type, startDate, endDate);
    }

    private KPIDTO getOfficeKPI(String type, LocalDate startDate, LocalDate endDate) {

        int totalSales = sumSales(startDate, endDate);

        LocalDateTime startDt = startDate.atStartOfDay();
        LocalDateTime endDt   = endDate.atTime(23, 59, 59);

        Integer totalMenuCount = salesOrderRepository.getTotalMenuCount(startDt, endDt);
        if (totalMenuCount == null) totalMenuCount = 0;

        int storeCount = storeDAO.countStores();
        int avgStoreSales = (storeCount == 0) ? 0 : (int) Math.round((double) totalSales / storeCount);

        double growthRate = calcWeeklyGrowthRate();

        return KPIDTO.builder()
                .totalSales(totalSales)
                .totalMenuCount(totalMenuCount)
                .avgStoreSales(avgStoreSales)
                .growthRate(growthRate)
                .build();
    }

    private double calcStoreWeeklyGrowth(Long storeNo) {
        LocalDate today = LocalDate.now();

        LocalDate thisStart = today.with(java.time.DayOfWeek.MONDAY);
        LocalDate thisEnd   = today.with(java.time.DayOfWeek.SUNDAY);

        int curr = sumStoreSales(storeNo, thisStart, thisEnd);

        LocalDate lastStart = thisStart.minusWeeks(1);
        LocalDate lastEnd   = thisEnd.minusWeeks(1);

        int prev = sumStoreSales(storeNo, lastStart, lastEnd);

        if (prev == 0) return 0.0;

        return ((double)(curr - prev) / prev) * 100.0;
    }

    private int sumStoreSales(Long storeNo, LocalDate start, LocalDate end) {
        return storeSalesRepository
                .findByStore_StoreNoAndSalesDateBetween(storeNo, start, end)
                .stream()
                .mapToInt(StoreSales::getSalesPrice)
                .sum();
    }

    private double calcWeeklyGrowthRate() {
        LocalDate today = LocalDate.now();

        LocalDate thisStart = today.with(java.time.DayOfWeek.MONDAY);
        LocalDate thisEnd   = today.with(java.time.DayOfWeek.SUNDAY);

        int curr = sumSales(thisStart, thisEnd);

        LocalDate lastStart = thisStart.minusWeeks(1);
        LocalDate lastEnd   = thisEnd.minusWeeks(1);

        int prev = sumSales(lastStart, lastEnd);

        if (prev == 0) return 0.0;

        return ((double)(curr - prev) / prev) * 100.0;
    }


    private int sumSales(LocalDate start, LocalDate end) {
        return storeSalesRepository
                .findBySalesDateBetween(start, end)
                .stream()
                .mapToInt(StoreSales::getSalesPrice)
                .sum();
    }

    private LocalDate convert(String type, String value, boolean isEnd) {

        switch (type) {
            case "day":
                return LocalDate.parse(value);

            case "week":
                String[] arr = value.split("-W");
                int year = Integer.parseInt(arr[0]);
                int week = Integer.parseInt(arr[1]);
                LocalDate startOfWeek = LocalDate.ofYearDay(year, 1)
                        .with(WeekFields.ISO.weekOfWeekBasedYear(), week)
                        .with(WeekFields.ISO.dayOfWeek(), 1);
                return isEnd ? startOfWeek.plusDays(6) : startOfWeek;

            case "month":
                YearMonth ym = YearMonth.parse(value);
                return isEnd ? ym.atEndOfMonth() : ym.atDay(1);

            case "year":
                int y = Integer.parseInt(value);
                return isEnd ? LocalDate.of(y, 12, 31) : LocalDate.of(y, 1, 1);

            default:
                throw new IllegalArgumentException("Invalid type: " + type);
        }
    }
}
