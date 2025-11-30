package com.erp.controller;

import com.erp.controller.exception.ItemOrderNotFoundException;
import com.erp.dto.ItemOrderDTO;
import com.erp.dto.ItemOrderDetailDTO;
import com.erp.service.ItemOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class ItemOrderRestController {
    @Autowired
    ItemOrderService itemOrderService;

    @GetMapping("/itemOrder/itemOrderList/{pageNo}")
    public Map<String, Object> itemOrderList(@PathVariable int pageNo) {
        Page<ItemOrderDTO> page = itemOrderService.getItemOrderList(pageNo);
        return Map.of(
                "list", page.getContent(),
                "totalPages", page.getTotalPages(),
                "pageNo", page.getNumber() + 1,
                "totalElement", page.getTotalElements()
        );
    }
    @GetMapping("/itemOrder/itemOrderDetail/{itemOrderNo}")
    public List<ItemOrderDetailDTO> itemOrderDetail(@PathVariable Long itemOrderNo) {
        return itemOrderService.getItemOrderDetailByOrderNo(itemOrderNo);
    }

    @PutMapping("/itemOrder/cancelItemOrder/{itemOrderNo}")
    public ResponseEntity<Map<String, String>> cancelItemOrder(@PathVariable Long itemOrderNo) {
        try {
            itemOrderService.cancelItemOrder(itemOrderNo);
        }
        catch (ItemOrderNotFoundException e) {
            System.err.println(e.getMessage());
            return ResponseEntity.status(400).build();
        }
        return ResponseEntity.ok().body(Map.of("message", "Cancel ItemOrder Success"));
    }
}
