package com.erp.controller;

import com.erp.auth.PrincipalDetails;
import com.erp.controller.exception.ItemOrderNotFoundException;
import com.erp.controller.exception.StoreItemNotFoundException;
import com.erp.controller.exception.StoreNotFoundException;
import com.erp.dao.StoreDAO;
import com.erp.dto.*;
import com.erp.repository.entity.ItemProposal;
import com.erp.service.ItemOrderService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
public class ItemOrderRestController {
    @Autowired
    ItemOrderService itemOrderService;
    @Autowired
    private StoreDAO storeDAO;

    @GetMapping("/itemOrder/itemOrderListAll")
    public Map<String, Object> itemOrderList(@RequestParam int pageNo) {
        Page<ItemOrderDTO> page = itemOrderService.getItemOrderList(pageNo);
        return Map.of(
                "list", page.getContent(),
                "totalPages", page.getTotalPages(),
                "pageNo", page.getNumber() + 1,
                "totalElement", page.getTotalElements()
        );
    }
    @GetMapping("/itemOrder/itemOrderListFilter")
    public Map<String, Object> itemOrderListFilter(@RequestParam int pageNo,
                                                 @RequestParam String startDate,
                                                 @RequestParam String endDate,
                                                 @RequestParam String orderStatus,
                                                 @AuthenticationPrincipal PrincipalDetails dp) {
        String managerId = dp.getManager().getManagerId();
        Long storeNo = storeDAO.getStoreNoByManager(managerId);
        if(storeNo == null) storeNo = 0L;

        Page<ItemOrderDTO> page = itemOrderService.getItemOrderList(pageNo, storeNo, orderStatus, startDate, endDate);
        return Map.of(
                "list", page.getContent(),
                "totalPages", page.getTotalPages(),
                "pageNo", page.getNumber() + 1,
                "totalElement", page.getTotalElements()
        );
    }
    @GetMapping("/itemOrder/itemOrderListFilter/{storeNo}")
    public Map<String, Object> itemOrderListFilter(@PathVariable Long storeNo,
                                                   @RequestParam int pageNo,
                                                   @RequestParam String startDate,
                                                   @RequestParam String endDate,
                                                   @RequestParam String orderStatus) {
        Page<ItemOrderDTO> page = itemOrderService.getItemOrderList(pageNo, storeNo, orderStatus, startDate, endDate);
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

    @PutMapping("/itemOrder/receiveItemOrder/{itemOrderDetailNo}")
    public ResponseEntity<Map<String, String>> receiveItemOrder(@PathVariable Long itemOrderDetailNo) {
        try{
            itemOrderService.receiveItem(itemOrderDetailNo);
        }
        catch (StoreItemNotFoundException e) {
            System.err.println(e.getMessage());
            return ResponseEntity.status(400).build();
        }
        return ResponseEntity.ok().body(Map.of("message", "Receive ItemOrderDetail Success"));
    }

    @GetMapping("/itemOrder/itemProposalHistory")
    public List<ItemProposalDTO> proposalItemOrderHistory(@AuthenticationPrincipal PrincipalDetails dp) {
        return itemOrderService.getItemProposalHistoryByStoreNo(dp.getStore().getStoreNo());
    }

    @GetMapping("/itemOrder/itemProposalHistory/{storeNo}")
    public List<ItemProposalDTO> proposalItemOrderHistory(@PathVariable Long storeNo) {
        return itemOrderService.getItemProposalHistoryByStoreNo(storeNo);
    }

    @GetMapping("/itemOrder/itemProposal")
    public List<ItemProposalDTO> proposalItemOrder(@AuthenticationPrincipal PrincipalDetails dp) {
        return itemOrderService.getItemProposalByStoreNo(dp.getStore().getStoreNo());
    }
    @GetMapping("/itemOrder/itemProposal/{storeNo}")
    public List<ItemProposalDTO> proposalItemOrder(@PathVariable Long storeNo) {
        return itemOrderService.getItemProposalByStoreNo(storeNo);
    }

    @PutMapping("/itemOrder/respondItemProposal/{proposalNo}")
    public ResponseEntity<Map<String, String>> responseProposal(@PathVariable Long proposalNo) {
        try {
            itemOrderService.responseProposal(proposalNo);
        }
        catch (EntityNotFoundException e) {
            System.err.println(e.getMessage());
            return ResponseEntity.status(400).build();
        }
        return ResponseEntity.ok().body(Map.of("message", "Response Proposal Success"));
    }

    @GetMapping("/itemOrder/itemList")
    public List<ItemStoreQuantityDTO> itemList(@AuthenticationPrincipal PrincipalDetails dp) {
        return itemOrderService.itemList(dp.getStore().getStoreNo());
    }
    @GetMapping("/itemOrder/itemList/{storeNo}")
    public List<ItemStoreQuantityDTO> itemList(@PathVariable Long storeNo) {
        return itemOrderService.itemList(storeNo);
    }

    @PostMapping("/itemOrder/itemOrder")
    public ResponseEntity<Map<String, String>> requestItemOrder(@RequestBody ItemOrderRequestDTO request, @AuthenticationPrincipal PrincipalDetails dp) {
        try {
            itemOrderService.requestItemOrder(request, dp.getStore().getStoreNo());
        }
        catch (Exception e) {
            System.err.println(e.getMessage());
            return ResponseEntity.status(400).build();
        }
        return ResponseEntity.ok().body(Map.of("message", "Request ItemOrder Success"));
    }

    @PutMapping("/itemOrder/approveItemOrder/{itemOrderNo}")
    public ResponseEntity<Map<String, String>> approveItemOrder(@PathVariable Long itemOrderNo) {
        try {
            itemOrderService.approveItemOrder(itemOrderNo, "galaxy0712");
        }
        catch (ItemOrderNotFoundException e) {
            System.err.println(e.getMessage());
            return ResponseEntity.status(400).build();
        }
        return ResponseEntity.ok().body(Map.of("message", "Approve ItemOrder Success"));
    }

    @PutMapping("/itemOrder/declineItemOrder/{itemOrderNo}")
    public ResponseEntity<Map<String, String>> declineItemOrder(@PathVariable Long itemOrderNo) {
        try {
            itemOrderService.declineItemOrder(itemOrderNo, "galaxy0712");
        }
        catch (ItemOrderNotFoundException e) {
            System.err.println(e.getMessage());
            return ResponseEntity.status(400).build();
        }
        return ResponseEntity.ok().body(Map.of("message", "Decline ItemOrder Success"));
    }


    @PostMapping("/itemOrder/propose")
    public ResponseEntity<Map<String, String>> proposalItemOrder(@RequestBody ProposalItemOrderDTO request) {
        try {
            itemOrderService.proposeItemOrder(request);
        }
        catch (Exception e) {
            System.err.println(e.getMessage());
            return ResponseEntity.status(400).build();
        }
        return ResponseEntity.ok().body(Map.of("message", "Propose ItemOrder Success"));
    }
}
