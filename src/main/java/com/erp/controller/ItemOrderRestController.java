package com.erp.controller;

import com.erp.auth.PrincipalDetails;
import com.erp.exception.ItemOrderNotFoundException;
import com.erp.exception.StoreItemNotFoundException;
import com.erp.dao.StoreDAO;
import com.erp.dto.*;
import com.erp.response.ApiResponse;
import com.erp.response.ErrorCode;
import com.erp.response.ErrorResponse;
import com.erp.service.ItemOrderService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RequestMapping("/api")
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
    @GetMapping("/itemOrder/itemOrderList/{pageNo}")
    public ApiResponse<?> itemOrderListFilter(@PathVariable int pageNo,
                                               @RequestParam String orderStatus,
                                               @RequestParam String startDate,
                                               @RequestParam String endDate,
                                               @AuthenticationPrincipal PrincipalDetails dp) {
        Long storeNo = storeDAO.getStoreNoByManager(dp.getManager().getManagerId());
        Page<ItemOrderDTO> page = itemOrderService.getItemOrderList(pageNo-1, storeNo, orderStatus, startDate, endDate);

        return ApiResponse.success(
            Map.of(
                "content", page.getContent(),
                "totalPages", page.getTotalPages(),
                "pageNo", page.getNumber(),
                "totalElements", page.getTotalElements()
            )
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
    public ApiResponse<List<ItemProposalDTO>> proposalItemOrder(@AuthenticationPrincipal PrincipalDetails dp) {
        long storeNo = storeDAO.getStoreNoByManager(dp.getManager().getManagerId());

        return ApiResponse.success(itemOrderService.getItemProposalByStoreNo(storeNo));
    }
    @GetMapping("/itemOrder/itemProposal/{storeNo}")
    public List<ItemProposalDTO> proposalItemOrder(@PathVariable Long storeNo) {
        return itemOrderService.getItemProposalByStoreNo(storeNo);
    }

    @PutMapping("/itemOrder/respondItemProposal/{proposalNo}")
    public ResponseEntity<?> responseProposal(@PathVariable Long proposalNo) {
        try {
            itemOrderService.responseProposal(proposalNo);
        }
        catch (EntityNotFoundException e) {
            return ResponseEntity.status(402).body(ApiResponse.error(ErrorResponse.of(ErrorCode.REQ_FAILED)));
        }
        return ResponseEntity.ok(ApiResponse.success(Map.of("message", "success")));
    }

    @GetMapping("/itemOrder/itemList")
    public ApiResponse<List<ItemStoreQuantityDTO>> itemList(@AuthenticationPrincipal PrincipalDetails dp) {
        Long storeNo = storeDAO.getStoreNoByManager(dp.getManager().getManagerId());


        return ApiResponse.success(itemOrderService.itemList(storeNo));
    }
    @GetMapping("/itemOrder/itemList/{storeNo}")
    public List<ItemStoreQuantityDTO> itemList(@PathVariable Long storeNo) {
        return itemOrderService.itemList(storeNo);
    }

    @PostMapping("/itemOrder/itemOrder")
    public ResponseEntity<?> requestItemOrder(@RequestBody ItemOrderRequestDTO request, @AuthenticationPrincipal PrincipalDetails dp) {
        long storeNo = storeDAO.getStoreNoByManager(dp.getManager().getManagerId());
        try {
            itemOrderService.requestItemOrder(request, storeNo);
        }
        catch (Exception e) {
            System.err.println(e.getMessage());
            return ResponseEntity.status(403).body(ApiResponse.error(ErrorResponse.of(ErrorCode.REQ_FAILED)));
        }
        return ResponseEntity.ok(ApiResponse.success(Map.of("message", "success")));
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
