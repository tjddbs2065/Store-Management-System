package com.erp.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class IndexController {

    @GetMapping("/loginView")
    public String loginView() {
        return "loginUI";   // 너의 loginUI.html 경로에 맞춰서
    }

    @GetMapping("/noPermission")
    public String noPermission() {
        return "noPermission";
    }
}
