package com.erp.controller;

import com.erp.auth.PrincipalDetails;
import com.erp.dto.MenuStatusDTO;
import com.erp.dto.StoreMenuDTO;
import com.erp.dto.StoreMenuGroupedDTO;
import com.erp.service.StoreMenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/storeMenu")
public class StoreMenuRestController {
    private final StoreMenuService storeMenuService;

    @GetMapping("/searchMenu")
    public List<StoreMenuGroupedDTO> searchMenu(
            @RequestParam String menuName,
            @RequestParam(required = false) String salesStatus,
            @RequestParam(required = false) String menuCategory
    ){
        return storeMenuService.searchMenu(menuName, salesStatus, menuCategory);
    }

    @PostMapping("/updateStatus")
    public ResponseEntity<Map<String, String>> updateStatus(
            @RequestBody List<MenuStatusDTO> updates) {

        storeMenuService.updateStatus(updates);

        return ResponseEntity.ok(Map.of("message", "success"));
    }
    @GetMapping("/getStoreMenu")
    public List<StoreMenuGroupedDTO> getStoreMenuListForManager(
            @RequestParam Long storeNo,
            @RequestParam(required = false) String salesStatus,
            @RequestParam(required = false, name = "menuCategory") String menuCategory
    ){
        return storeMenuService.getStoreMenuList(
                storeNo,
                null,
                salesStatus,
                menuCategory
        );
    }


    @GetMapping("/getMenu")
    public List<StoreMenuGroupedDTO> getStoreMenuList(
            @AuthenticationPrincipal PrincipalDetails principal,
            @RequestParam(required = false) String menuName,
            @RequestParam(required = false) String salesStatus,
            @RequestParam(required = false) String menuCategory ){

        Long storeNo = principal.getStore().getStoreNo();
        return storeMenuService.getStoreMenuList( storeNo, menuName, salesStatus, menuCategory );
    }

    @GetMapping("/getStoreMenu/{storeNo}")
    public List<StoreMenuDTO> getStoreMenu(@PathVariable Long storeNo) {
        return storeMenuService.getStoreMenu(storeNo);
    }
}
