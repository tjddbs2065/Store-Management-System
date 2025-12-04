package com.erp.controller;

import com.erp.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/item")
public class ItemController {

    private final ItemService itemService;

    /** 권한 → MANAGER / STORE 문자열로 정규화 */
    private String resolveRole(Authentication auth) {
        if (auth == null) return "STORE";
        for (GrantedAuthority a : auth.getAuthorities()) {
            String r = a.getAuthority(); // ROLE_MANAGER, MANAGER 등
            if (r != null && r.toUpperCase().contains("MANAGER")) return "MANAGER";
        }
        return "STORE";
    }

    /** 목록 화면 (본사/직영점 모두 허용) */
    @GetMapping("/get")
    public String itemGet(Authentication auth, Model model) {
        model.addAttribute("role", resolveRole(auth));
        return "item/itemUI";
    }

    /** 상세 화면 (본사/직영점 모두 허용) */
    @GetMapping("/detail")
    public String itemDetail(@RequestParam Long itemNo, Authentication auth, Model model) {
        model.addAttribute("role", resolveRole(auth));
        model.addAttribute("itemNo", itemNo);
        return "item/itemDetailUI";
    }

    /** 등록 화면 (ADMIN / MANAGER만) */
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @GetMapping("/add")
    public String itemAdd(Authentication auth, Model model) {
        model.addAttribute("role", resolveRole(auth));
        return "item/itemAddUI";
    }

    /** 수정 화면 (ADMIN / MANAGER만) */
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @GetMapping("/set")
    public String itemSet(@RequestParam Long itemNo, Authentication auth, Model model) {
        model.addAttribute("role", resolveRole(auth));
        model.addAttribute("itemNo", itemNo);
        return "item/itemSetUI";
    }
}
