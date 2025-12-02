package com.erp.controller;

import com.erp.dto.SalesOrderDTO;
import com.erp.dto.SalesOrderRequestDTO;
import com.erp.service.SalesOrderService;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/sales/salesOrder")
public class SalesOrderRestController {
    private final SalesOrderService salesOrderService;

    @GetMapping("/salesOrderList/{pageNo}")
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
    @GetMapping("/salesOrderDetail/{salesOrderNo}")
    public Map<String, Object> getSalesOrderDetail(
            @PathVariable Long salesOrderNo
    ){
        return salesOrderService.getSalesOrderDetail(salesOrderNo);
    }

    @PostMapping("/addSalesOrder")
    public ResponseEntity<Map<String, Object>> addSalesOrder(@RequestBody SalesOrderRequestDTO request) {
        salesOrderService.addSalesOrder(request);

        return ResponseEntity.ok().body(Map.of("message", "Request addSalesOrder success"));
    }

}
