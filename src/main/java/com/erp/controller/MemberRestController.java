package com.erp.controller;

import com.erp.exception.ManagerException;
import com.erp.dto.AddStoreRequestDTO;
import com.erp.dto.ManagerDTO;
import com.erp.dto.StoreDTO;
import com.erp.service.MemberService;
import com.erp.service.StoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/member")
public class MemberRestController {

    private final MemberService memberService;
    private final StoreService storeService;

    @PutMapping("/manager")
    public ResponseEntity<Map<String, String>> updateManager(@RequestBody ManagerDTO requestBody) {
        memberService.setManager(requestBody);
        return ResponseEntity.ok().body(Map.of("message", "success"));
    }

    @PutMapping(value = "/store", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> updateStore(
            @RequestPart AddStoreRequestDTO request,
            @RequestPart(value="storeImage", required = false) MultipartFile storeImage) {
        ManagerDTO manager = ManagerDTO.toDTO(request.getManager());
        StoreDTO store = StoreDTO.toDTO(request.getStore());

        storeService.setStore(manager, store, storeImage);
        return ResponseEntity.ok().body(Map.of("message", "직영점 변경 성공"));
    }

    @GetMapping("/manager/{managerId}")
    public ResponseEntity<Map<String, Object>> getManager(@PathVariable String managerId) {
        ManagerDTO managerDTO = memberService.getManager(managerId);

        return ResponseEntity.ok().body(Map.of("manager", managerDTO));
    }

    @GetMapping("/store/{managerId}")
    public ResponseEntity<Map<String, Object>> getStore(@PathVariable String managerId) {
        ManagerDTO managerDTO = memberService.getManager(managerId);
        StoreDTO storeDTO =  storeService.getStore(managerId);

        Map<String, Object> data = new HashMap<>();
        data.put("manager", managerDTO);
        data.put("store", storeDTO);

        return ResponseEntity.ok().body(data);
    }

    @PostMapping("/manager")
    public ResponseEntity<Map<String, String>> manager(@RequestBody AddStoreRequestDTO request, Model model) {
        ManagerDTO manager = ManagerDTO.toDTO(request.getManager());

        memberService.addManager(manager);

        return ResponseEntity.ok().body(Map.of("message", "직원 추가 성공"));
    }

    @PostMapping(value = "/store", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> store(
            @RequestPart AddStoreRequestDTO request,
            @RequestPart(value="storeImage", required = false) MultipartFile storeImage) {
        ManagerDTO manager = ManagerDTO.toDTO(request.getManager());
        StoreDTO store = StoreDTO.toDTO(request.getStore());

        storeService.addStore(manager, store, storeImage);
        return ResponseEntity.ok().body(Map.of("message", "직영점 추가 성공"));
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
     * 직영점 메뉴 판매중단 권한 변경
     * POST /admin/member/store/menuStopRole
     * JSON: { "storeNo": 1, "menuStopRole": "Y" }
     */
    @PostMapping("/store/menuStopRole")
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, String> setStoreMenuStopRole(@RequestBody StoreDTO dto) {
        memberService.setStoreMenuStopRole(dto.getStoreNo(), dto.getMenuStopRole());
        return Map.of("message", "ok");
    }



}
