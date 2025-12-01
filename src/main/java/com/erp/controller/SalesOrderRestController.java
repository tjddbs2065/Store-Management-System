package com.erp.controller;

import com.erp.dto.SalesOrderDTO;
import com.erp.service.SalesOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class SalesOrderRestController {
    private final SalesOrderService salesOrderService;

    @GetMapping("/salesOrder/salesOrderList/{pageNo}")
    public Map<String, Object> getSalesOrderList(
            @PathVariable int pageNo,
            @RequestParam(required=false) LocalDate date,
            @RequestParam(required=false) String storeName
    ) {

        Page<SalesOrderDTO> page =
                salesOrderService.getSalesOrderList(pageNo - 1, date, storeName);

        return Map.of(
                "list", page.getContent(),
                "totalPages", page.getTotalPages(),
                "pageNo", page.getNumber() + 1,
                "totalElements", page.getTotalElements()
        );
    }
    @GetMapping("/salesOrder/salesOrderDetail/{salesOrderNo}")
    public Map<String, Object> getSalesOrderDetail(
            @PathVariable Long salesOrderNo
    ){
        return salesOrderService.getSalesOrderDetail(salesOrderNo);
    }


}
