package com.erp.controller;

import com.erp.dto.ItemOrderDTO;
import com.erp.service.ItemOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/itemOrder")
public class ItemOrderController {
    private ItemOrderService itemOrderService;

    @Secured("ROLE_STORE")
    @GetMapping("/itemOrderList")
    public String itemOrderHistory() {
        return "itemOrder/itemOrderHistory";
    }
    @Secured("ROLE_STORE")
    @GetMapping("/itemOrder")
    public String itemOrder() {
        return "itemOrder/itemOrder";
    }
    @Secured("ROLE_MANAGER")
    @GetMapping("/itemPropose")
    public String itemProposal() {
        return "itemOrder/itemProposal";
    }
    @Secured("ROLE_MANAGER")
    @GetMapping("/itemOrderListManager")
    public String itemOrderHistoryManager() {
        return "itemOrder/itemOrderHistoryManager";
    }
}
