package com.erp.controller;

import com.erp.dto.StoreDTO;
import com.erp.service.StoreSearchModalService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/storeSearch/modal")
public class StoreSearchModalRestController {

    private final StoreSearchModalService storeSearchModalService;

    // 모달: 직영점 목록 (키워드 없으면 전체, 이름 LIKE 검색)
    @GetMapping
    public List<StoreDTO> getStores(@RequestParam(required = false) String keyword) {
        return storeSearchModalService.getStores(keyword);
    }
}
