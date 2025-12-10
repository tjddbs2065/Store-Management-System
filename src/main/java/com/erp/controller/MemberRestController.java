package com.erp.controller;

import com.erp.dto.ManagerDTO;
import com.erp.dto.StoreDTO;
import com.erp.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/member")
public class MemberRestController {

    private final MemberService memberService;

    /**
     * 본사 직원 목록
     */
    @GetMapping("/manager")
    @PreAuthorize("hasRole('ADMIN')")
    public Page<ManagerDTO> getManagerMembers(
            @RequestParam(defaultValue = "0") Integer page) {

        int safePage = (page == null ? 0 : page);
        return memberService.getManagerMembers(safePage);
    }

    /**
     * 직영점 직원 목록
     */
    @GetMapping("/store")
    @PreAuthorize("hasRole('ADMIN')")
    public Page<StoreDTO> getStoreMembers(
            @RequestParam(defaultValue = "0") Integer page) {

        int safePage = (page == null ? 0 : page);
        return memberService.getStoreMembers(safePage);
    }

    /**
     * 직영점 메뉴 판매 중지 권한 변경
     * PATCH /admin/member/store/menuStopRole
     * JSON: { "storeNo": 1, "menuStopRole": "Y" }
     */
    @PatchMapping("/store/menuStopRole")
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, String> updateStoreMenuStopRole(@RequestBody StoreDTO dto) {
        memberService.updateStoreMenuStopRole(dto.getStoreNo(), dto.getMenuStopRole());
        return Map.of("message", "ok");
    }
}
