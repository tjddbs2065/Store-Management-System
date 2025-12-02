package com.erp.controller;

import com.erp.dto.*;
import com.erp.service.SalesChartService;
import com.erp.service.SalesKPIService;
import com.erp.service.SalesListService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class SalesRestController {

    private final SalesChartService salesChartService;
    private final SalesKPIService salesKPIService;
    private final SalesListService salesListService;

    @GetMapping("/salesDetail")
    public List<StoreDailyMenuSalesDTO> getSalesDetail(
            @RequestParam Long storeNo,
            @RequestParam String salesDate
    ) {
        return salesListService.getSalesDetail(storeNo, LocalDate.parse(salesDate));
    }

    @GetMapping("/salesList")
    public Map<String, Object> getSalesList(
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestParam(required = false) String storeName,
            @RequestParam(defaultValue = "1") int page     // UI에서 보낸 page 번호 (1-based)
    ) {

        Pageable pageable = PageRequest.of(page - 1, 10); // 10개 고정

        Page<SalesListDTO> result =
                salesListService.getSalesList(startDate, endDate, storeName, pageable);

        Map<String, Object> res = new HashMap<>();
        res.put("list", result.getContent());
        res.put("totalPages", result.getTotalPages());
        res.put("currentPage", page);

        return res;
    }

    @GetMapping("/KPI")
    public KPIDTO getKPI(
            @RequestParam String type,
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestParam(required = false) Long storeNo
    ) {
        return salesKPIService.getKPIByDate(type, startDate, endDate, storeNo);
    }

    @GetMapping("salesChart")
    public SalesChartDTO getSalesChart(
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestParam String type,
            @RequestParam(required = false) Long storeNo

    ) {
        return salesChartService.getSalesChartByDate(startDate,endDate,type, storeNo);
    }

    @GetMapping("/totalStoreSales")
    public List<TotalStoreSalesDTO> getTop5StoreSales() {
        return salesChartService.getTotalStoreSales();
    }

    @GetMapping("/menuRatio")
    public List<MenuRatioDTO> getMenuRatio() {
        return salesChartService.getMenuRatio();
    }
}
