package com.erp.controller;

import com.erp.dto.StoreDTO;
import com.erp.service.StoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class StoreListRestController {
    private final StoreService storeService;

    @GetMapping("/getStoreList")
    public Page<StoreDTO> getStoreList(@RequestParam(defaultValue = "0") Integer page,
                                       @RequestParam(required = false) String address,
                                       @RequestParam(required = false) String storeName,
                                       @RequestParam(required = false) String managerName,
                                       @RequestParam(required = false) String storeStatus) {
        return storeService.getStoresList(page, address, storeName, managerName, storeStatus);
    }

    @GetMapping("/getStoreDetail/{storeNo}")
    public StoreDTO getStoreDetail(@PathVariable Long storeNo) {
        return storeService.getStoreDetail(storeNo);
    }
}
