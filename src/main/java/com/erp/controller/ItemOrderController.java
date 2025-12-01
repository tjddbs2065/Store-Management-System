package com.erp.controller;

import com.erp.dto.ItemOrderDTO;
import com.erp.service.ItemOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class ItemOrderController {
    private ItemOrderService itemOrderService;

    @GetMapping("/itemOrder/itemOrderList")
    public String itemOrderHistory() {
        return "/itemOrder/itemOrderHistory";
    }
    @GetMapping("/itemOrder/itemOrder")
    public String itemOrder() {
        return "/itemOrder/itemOrder";
    }
    @GetMapping("/itemOrder/itemProposal")
    public String itemProposal() {
        return "/itemOrder/itemProposal";
    }
    @GetMapping("/itemOrder/itemOrderListManager")
    public String itemOrderHistoryManager() {
        return "/itemOrder/itemOrderListManager";
    }
}
