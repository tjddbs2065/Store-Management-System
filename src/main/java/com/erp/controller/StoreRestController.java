package com.erp.controller;

import com.erp.controller.exception.ManagerException;
import com.erp.dto.AddStoreRequestDTO;
import com.erp.dto.ManagerDTO;
import com.erp.dto.StoreDTO;
import com.erp.service.MemberService;
import com.erp.service.StoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class StoreRestController {

    private final StoreService storeService;
    private final MemberService memberService;
    private final BCryptPasswordEncoder encoder;

    @PostMapping("/manager")
    public ResponseEntity<Map<String, String>> manager(@RequestBody AddStoreRequestDTO request, Model model) {
        System.out.println(request.getManager());
        ManagerDTO manager = ManagerDTO.toDTO(request.getManager());

        memberService.addManager(manager);

        return ResponseEntity.ok().body(Map.of("message", "Add Manager Success"));
    }

    @PostMapping("/store")
    public ResponseEntity<Map<String, String>> store(@RequestBody AddStoreRequestDTO request, Model model) {
        ManagerDTO manager = ManagerDTO.toDTO(request.getManager());
        StoreDTO store = StoreDTO.toDTO(request.getStore());

        storeService.addStore(manager, store);
        return ResponseEntity.ok().body(Map.of("message", "Add Store Success"));
    }

    @GetMapping("/member")
    public ResponseEntity<Map<String, String>> member(@RequestParam String managerId) {
        try {
            memberService.checkManager(managerId);
        }
        catch (ManagerException e){
            return ResponseEntity.ok().body(Map.of("message", "사용가능"));
        }

        return ResponseEntity.badRequest().body(Map.of("message", "이미 있는 아이디 입니다."));
    }
}
