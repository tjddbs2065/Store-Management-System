package com.erp.controller;

import com.erp.controller.request.StoreItemSearchRequestDTO;
import com.erp.dto.PageResponseDTO;
import com.erp.dto.StoreItemDTO;
import com.erp.service.StoreItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/stock/storeItem")
public class StoreItemRestController {

    private final StoreItemService storeItemService;

    /**
     * 재고 조회 : 본사용 목록 API
     *  - GET /stock/storeItem/manager/list/{pageNo}?storeNo=1&category=치즈&searchType=NAME&keyword=모짜
     *  - pageNo : 1-base (UI) → 서비스는 0-base
     */
    @GetMapping("/manager/list/{pageNo}")
    public PageResponseDTO<StoreItemDTO> getManagerItems(@PathVariable int pageNo,
                                                         StoreItemSearchRequestDTO request) {
        request.setPage(pageNo - 1);   // 1 → 0, 2 → 1 ...
        return storeItemService.getStoreItems(request);
    }

    /**
     * 재고 조회 : 직영점용 목록 API
     *  - GET /stock/storeItem/store/list/{pageNo}?storeNo=1&category=치즈&searchType=NAME&keyword=모짜
     */
    @GetMapping("/store/list/{pageNo}")
    public PageResponseDTO<StoreItemDTO> getStoreItems(@PathVariable int pageNo,
                                                       StoreItemSearchRequestDTO request) {
        request.setPage(pageNo - 1);
        return storeItemService.getStoreItems(request);
    }

    /**
     * 하한선 저장 (본사/직영점 공용)
     *  - POST /stock/storeItem/{storeItemNo}/limit
     *  - form-data 또는 x-www-form-urlencoded:
     *      newLimit     : Integer 또는 비워두면 하한선 삭제
     *      isManagerRole: true(본사), false(직영점)
     */
    @PostMapping("/{storeItemNo}/limit")
    public void updateLimit(@PathVariable Long storeItemNo,
                            @RequestParam(required = false) Integer newLimit,
                            @RequestParam(defaultValue = "false") boolean isManagerRole) {
        storeItemService.setStoreItemLimit(storeItemNo, newLimit, isManagerRole);
    }
}
