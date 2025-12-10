package com.erp.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MemberController {

    /**
     * 직원 목록 UI
     * /admin/** 는 SecurityConfig 에서 ROLE_ADMIN 으로 제한되어 있음
     */
    @GetMapping("/admin/memberListUI")
    @PreAuthorize("hasRole('ADMIN')")
    public String memberListUI() {
        return "member/memberListUI";
    }
}
