package com.erp.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@RequiredArgsConstructor
@Controller
public class storeMenuController {

    @GetMapping("/store/storeMenu")
    private String storeMenu(){
        return "menu/storeMenuUI";
    }
}
