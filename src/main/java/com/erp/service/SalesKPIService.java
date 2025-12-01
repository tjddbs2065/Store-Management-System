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


    public KPIDTO getStoreKPI(Long storeNo, String type, String start, String end) {

        LocalDate startDate = convert(type, start, false);
        LocalDate endDate   = convert(type, end, true);

        int totalSales = sumStoreSales(storeNo, startDate, endDate);


        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime   = endDate.atTime(23, 59, 59);

        Integer totalMenuCount = salesOrderRepository.getStoreMenuCount(storeNo, startDateTime, endDateTime);
        if(totalMenuCount == null) totalMenuCount = 0;


        int avgPrice = (totalMenuCount == 0) ? 0 : (totalSales / totalMenuCount);

        double growthRate = calcStoreWeeklyGrowth(storeNo);

        return KPIDTO.builder()
                .totalSales(totalSales)
                .totalMenuCount(totalMenuCount)
                .avgOrderAmount(avgPrice)
                .growthRate(growthRate)
                .build();
    }


    private int sumStoreSales(Long storeNo, LocalDate start, LocalDate end) {
        return storeSalesRepository
                .findByStore_StoreNoAndSalesDateBetween(storeNo, start, end)
                .stream()
                .mapToInt(StoreSales::getSalesPrice)
                .sum();
    }



    private double calcStoreWeeklyGrowth(Long storeNo) {

        LocalDate today = LocalDate.now();

        LocalDate thisWeekStart = today.with(java.time.DayOfWeek.MONDAY);
        LocalDate thisWeekEnd   = today.with(java.time.DayOfWeek.SUNDAY);

        int thisWeekSales = sumStoreSales(storeNo, thisWeekStart, thisWeekEnd);

        LocalDate lastWeekStart = thisWeekStart.minusWeeks(1);
        LocalDate lastWeekEnd   = thisWeekEnd.minusWeeks(1);

        int lastWeekSales = sumStoreSales(storeNo, lastWeekStart, lastWeekEnd);

        if(lastWeekSales == 0) return 0.0;

        return ((double)(thisWeekSales - lastWeekSales) / lastWeekSales) * 100.0;
    }






    public KPIDTO getKPIByDate(String type, String start, String end) {
        LocalDate startDate = convert(type, start, false);
        LocalDate endDate   = convert(type, end, true);
        return getKpi(type, startDate, endDate);
    }


    public KPIDTO getKpi(String type, LocalDate startDate, LocalDate endDate) {

        int totalSales = sumSales(startDate, endDate);
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime   = endDate.atTime(23, 59, 59);

        Integer totalMenuCount = salesOrderRepository.getTotalMenuCount(startDateTime, endDateTime);
        if (totalMenuCount == null) totalMenuCount = 0;
        int storeCount = storeDAO.countStores();
        int avgStoreSales = storeCount == 0
                ? 0
                : (int) Math.round((double) totalSales / storeCount);


        double growthRate = calcWeeklyGrowthRate();
        return KPIDTO.builder()
                .totalSales(totalSales)
                .totalMenuCount(totalMenuCount)
                .avgStoreSales(avgStoreSales)
                .growthRate(growthRate)
                .build();
    }

    private double calcWeeklyGrowthRate() {

        LocalDate today = LocalDate.now();
        LocalDate thisWeekStart = today.with(java.time.DayOfWeek.MONDAY);
        LocalDate thisWeekEnd   = today.with(java.time.DayOfWeek.SUNDAY);
        int thisWeekSales = sumSales(thisWeekStart, thisWeekEnd);
        LocalDate lastWeekStart = thisWeekStart.minusWeeks(1);
        LocalDate lastWeekEnd   = thisWeekEnd.minusWeeks(1);
        int lastWeekSales = sumSales(lastWeekStart, lastWeekEnd);
        if (lastWeekSales == 0) return 0.0;
        return ((double)(thisWeekSales - lastWeekSales) / lastWeekSales) * 100.0;
    }


    private int sumSales(LocalDate startDate, LocalDate endDate) {
        return storeSalesRepository.findBySalesDateBetween(startDate, endDate)
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
