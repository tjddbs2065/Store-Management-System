package com.erp.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class StoreController {

    @GetMapping("/storeListUI")
    public String storeListUI() {
        return "member/storeListUI";
    }

    @GetMapping("/userJoin")
    public String userJoin() {return "member/memberAddUI";}
}
