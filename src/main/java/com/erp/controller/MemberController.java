package com.erp.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import com.erp.controller.exception.StoreNotFoundException;
import com.erp.dto.StoreDTO;
import com.erp.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;


    @Value("${juso.addrKey.key}")
    private String addrKey;

    @GetMapping("/admin/managerSetUI/{managerId}")
    public String managerSetUI() {
        return "member/managerSetUI";
    }
    @GetMapping("/admin/storeSetUI/{managerId}")
    public String storeSetUI() {
        return "member/storeSetUI";
    }

    /**
     * 직원 목록 UI
     * /admin/** 는 SecurityConfig 에서 ROLE_ADMIN 으로 제한되어 있음
     */
    @GetMapping("/admin/memberListUI")
    @PreAuthorize("hasRole('ADMIN')")
    public String memberListUI() {
        return "member/memberListUI";
    }

    /**
     * 직영점 상세보기 UI
     * 예) /admin/storeDetailUI?storeNo=1&fromTab=store&fromPage=2
     */
    @GetMapping("/admin/storeDetailUI")
    @PreAuthorize("hasRole('ADMIN')")
    public String storeDetailUI(@RequestParam("storeNo") long storeNo,
                                @RequestParam(name = "fromTab",  required = false, defaultValue = "store") String fromTab,
                                @RequestParam(name = "fromPage", required = false, defaultValue = "1")     Integer fromPage,
                                Model model) {

        StoreDTO store = memberService.getStoreDetail(storeNo);
        if (store == null) {
            throw new StoreNotFoundException("직영점 정보를 찾을 수 없습니다. storeNo=" + storeNo);
        }

        model.addAttribute("store", store);
        model.addAttribute("fromTab", fromTab);
        model.addAttribute("fromPage", fromPage);

        return "member/storeDetailUI";
    }

    @GetMapping("/admin/memberAddUI")
    public String userJoin() {return "member/memberAddUI";}

    @GetMapping("/jusoPopup")
    public String jusoPopup(Model model) {
        model.addAttribute("confmKey", addrKey);
        model.addAttribute("returnUrl", "http://211.108.241.166/jusoCallback");
        model.addAttribute("resultType", "json");
        return "member/jusoPopup";
    }

    @PostMapping("/jusoCallback")
    public String callback(HttpServletRequest request, Model model) {
        model.addAttribute("roadAddr", request.getParameter("roadAddr"));
        model.addAttribute("roadFullAddr", request.getParameter("roadFullAddr"));
        model.addAttribute("zipNo", request.getParameter("zipNo"));
        model.addAttribute("jibunAddr", request.getParameter("jibunAddr"));
        model.addAttribute("admCd", request.getParameter("admCd"));
        model.addAttribute("rnMgtSn", request.getParameter("rnMgtSn"));
        model.addAttribute("buldMnnm", request.getParameter("buldMnnm"));
        model.addAttribute("buldSlno", request.getParameter("buldSlno"));

        return "member/jusoCallback";
    }
}
