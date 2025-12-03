package com.erp.controller;

import com.erp.dto.ItemDTO;
import com.erp.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/items")
public class ItemRestController {

    private final ItemService itemService;

    /** 단건 조회 (모두 허용) */
    @GetMapping("/{itemNo}")
    public ResponseEntity<ItemDTO> findOne(@PathVariable Long itemNo){
        ItemDTO dto = itemService.getDetail(itemNo);
        return (dto == null) ? ResponseEntity.notFound().build() : ResponseEntity.ok(dto);
    }

    /** 목록/검색 (모두 허용) */
    @GetMapping
    public ResponseEntity<List<ItemDTO>> find(
            @RequestParam(required = false) String category,
            @RequestParam(required = false, name="name") String itemName,
            @RequestParam(required = false, name="code") String itemCode,
            @RequestParam(required = false, name="ingredient") String ingredient
    ){
        if (category != null && !category.isBlank())   return ResponseEntity.ok(itemService.getItemsByCategory(category));
        if (itemName != null && !itemName.isBlank())   return ResponseEntity.ok(itemService.getItemsByItemName(itemName));
        if (itemCode != null && !itemCode.isBlank())   return ResponseEntity.ok(itemService.getItemsByItemCode(itemCode));
        if (ingredient != null && !ingredient.isBlank()) return ResponseEntity.ok(itemService.getItemsByIngredient(ingredient));
        return ResponseEntity.ok(itemService.getItemList(category, ingredient, itemCode));
    }

    /** 등록 (ADMIN/MANAGER만) */
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @PostMapping
    public ResponseEntity<?> create(@RequestBody ItemDTO dto){
        int r = itemService.addItem(dto);
        if (r != 1) return ResponseEntity.badRequest().body("등록 실패");
        return ResponseEntity.created(URI.create("/api/items/" + dto.getItemNo())).body(dto);
    }

    /** 수정 (ADMIN/MANAGER만) */
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @PutMapping("/{itemNo}")
    public ResponseEntity<?> update(@PathVariable Long itemNo, @RequestBody ItemDTO dto){
        dto.setItemNo(itemNo);
        int r = itemService.setItem(dto);
        return (r == 1) ? ResponseEntity.ok(dto) : ResponseEntity.badRequest().body("수정 실패");
    }

    /** 삭제(소프트) (ADMIN/MANAGER만) */
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @DeleteMapping("/{itemNo}")
    public ResponseEntity<?> delete(@PathVariable Long itemNo){
        int r = itemService.removeItem(itemNo);
        return (r == 1) ? ResponseEntity.ok().build() : ResponseEntity.badRequest().body("삭제 실패");
    }
}
