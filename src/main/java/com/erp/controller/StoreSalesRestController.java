package com.erp.controller;

import com.erp.auth.PrincipalDetails;
import com.erp.dto.*;
import com.erp.service.SalesChartService;
import com.erp.service.SalesKPIService;
import com.erp.service.SalesListService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class StoreSalesRestController {

    private final SalesChartService salesChartService;
    private final SalesKPIService salesKPIService;
    private final SalesListService salesListService;

    @GetMapping("/store/menuRatio")
    public List<MenuRatioDTO> getMenuRatio(
            @AuthenticationPrincipal PrincipalDetails principal,
            @RequestParam String startDate,
            @RequestParam String endDate
    ){
        Long storeNo = principal.getStore().getStoreNo();

        return salesChartService.getMenuRatio(startDate, endDate, storeNo);
    }

    @GetMapping("/store/salesList")
    public Map<String, Object> getSalesListForStore(
            @AuthenticationPrincipal PrincipalDetails principal,
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestParam(defaultValue = "1") int page
    ) {

        Long storeNo = principal.getStore().getStoreNo();

        Pageable pageable = PageRequest.of(page - 1, 10);

        Page<SalesListDTO> result =
                salesListService.getSalesListForStore(storeNo, startDate, endDate, pageable);

        Map<String, Object> res = new HashMap<>();
        res.put("list", result.getContent());
        res.put("totalPages", result.getTotalPages());
        res.put("currentPage", page);

        return res;
    }



    @GetMapping("/store/KPI")
    public KPIDTO getStoreKpi(
            @AuthenticationPrincipal PrincipalDetails principal,
            String startDate,
            String endDate,
            String type
    ) {
        Long storeNo = principal.getStore().getStoreNo();

        return salesKPIService.getKPIByDate(
                type,
                startDate,
                endDate,
                storeNo
        );
    }

    @GetMapping("/store/salesChart")
    public SalesChartDTO getSalesChart(
            @AuthenticationPrincipal PrincipalDetails principal,
            String startDate,
            String endDate,
            String type
    ) {

        Long storeNo = principal.getStore().getStoreNo();

        return salesChartService.getSalesChartByStore(
                storeNo,
                startDate,
                endDate,
                type
        );
    }

}