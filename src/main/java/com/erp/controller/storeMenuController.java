package com.erp.controller;

import com.erp.auth.PrincipalDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@RequiredArgsConstructor
@Controller
public class storeMenuController {

    @GetMapping("/store/storeMenu")
    private String storeMenu(
            Model model,
            @AuthenticationPrincipal PrincipalDetails principal){
        String storeName = principal.getStore().getStoreName();
        Long storeNo = principal.getStore().getStoreNo();
        model.addAttribute("storeNo", storeNo);
        model.addAttribute("storeName", storeName);
        return "menu/storeMenuUI";
    }

    @GetMapping("/manager/storeMenu")
    private String storeMenuManager(){
        return "menu/storeMenuManagerUI";
    }
}
