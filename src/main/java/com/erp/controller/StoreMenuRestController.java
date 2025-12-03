package com.erp.controller;

import com.erp.auth.PrincipalDetails;
import com.erp.dto.StoreMenuDTO;
import com.erp.dto.StoreMenuGroupedDTO;
import com.erp.service.StoreMenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/storeMenu")
public class StoreMenuRestController {
    private final StoreMenuService storeMenuService;

    @GetMapping("/getMenu")
    public List<StoreMenuGroupedDTO> getStoreMenuList(
            @AuthenticationPrincipal PrincipalDetails principal,
            @RequestParam(required = false) String menuName,
            @RequestParam(required = false) String salesStatus,
            @RequestParam(required = false) String menuCategory ){

        Long storeNo = principal.getStoreNo();
        return storeMenuService.getStoreMenuList( storeNo, menuName, salesStatus, menuCategory );
    }

    @GetMapping("/getStoreMenu/{storeNo}")
    public List<StoreMenuDTO> getStoreMenu(@PathVariable Long storeNo) {
        return storeMenuService.getStoreMenu(storeNo);
    }
}
