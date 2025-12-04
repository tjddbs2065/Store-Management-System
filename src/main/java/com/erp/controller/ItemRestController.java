package com.erp.controller;

import com.erp.dto.ItemDTO;
import com.erp.dto.PageResponseDTO;
import com.erp.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/items")
public class ItemRestController {

    private final ItemService itemService;

    /** 단건 조회 (모두 허용) */
    @GetMapping("/{itemNo}")
    public ResponseEntity<ItemDTO> findOne(@PathVariable Long itemNo) {
        ItemDTO dto = itemService.getDetail(itemNo);
        return (dto == null) ? ResponseEntity.notFound().build() : ResponseEntity.ok(dto);
    }

    /**
     * 목록/검색 + 페이징
     * JS: /api/items/list/{page}?size=&itemCategory=&itemName=&itemCode=&ingredientName=&supplier=
     */
    @GetMapping("/list/{page}")
    public PageResponseDTO<ItemDTO> listPaged(
            @PathVariable int page,                          // 1-base
            @RequestParam(required = false) String itemCategory,
            @RequestParam(required = false) String itemName,
            @RequestParam(required = false) String itemCode,
            @RequestParam(required = false) String ingredientName,
            @RequestParam(required = false) String supplier,
            @RequestParam(required = false, defaultValue = "10") Integer size
    ) {
        // 1) 검색 조건에 따라 전체 리스트 조회 (기존 서비스 그대로 사용)
        List<ItemDTO> list;

        if (itemName != null && !itemName.isBlank()) {
            list = itemService.getItemsByItemName(itemName);
        } else if (itemCode != null && !itemCode.isBlank()) {
            list = itemService.getItemsByItemCode(itemCode);
        } else if (ingredientName != null && !ingredientName.isBlank()) {
            list = itemService.getItemsByIngredient(ingredientName);
        } else if (itemCategory != null && !itemCategory.isBlank()) {
            list = itemService.getItemsByCategory(itemCategory);
        } else {
            list = itemService.getItemList(null, null, null);
        }

        // 공급사 검색은 메모리 필터링
        if (supplier != null && !supplier.isBlank()) {
            String kw = supplier.trim();
            List<ItemDTO> filtered = new ArrayList<>();
            for (ItemDTO dto : list) {
                if (dto.getSupplier() != null && dto.getSupplier().contains(kw)) {
                    filtered.add(dto);
                }
            }
            list = filtered;
        }

        // 2) 페이징 계산
        int pageSize = (size == null || size < 1) ? 10 : size;
        int totalElements = list.size();
        int totalPages = (totalElements == 0) ? 1 :
                (int) Math.ceil(totalElements / (double) pageSize);

        int currentPage = (page < 1) ? 1 : page;
        if (currentPage > totalPages) currentPage = totalPages;

        int fromIndex = (currentPage - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, totalElements);

        List<ItemDTO> pageContent =
                (fromIndex >= totalElements) ? List.of() : list.subList(fromIndex, toIndex);

        // 페이징 블록 (5개)
        int blockSize = 5;
        int blockIdx = (currentPage - 1) / blockSize;
        int startPage = blockIdx * blockSize + 1;
        int endPage = Math.min(totalPages, startPage + blockSize - 1);

        return PageResponseDTO.<ItemDTO>builder()
                .content(pageContent)
                .page(currentPage - 1)   // 0-base
                .size(pageSize)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .startPage(startPage)
                .endPage(endPage)
                .hasPrevBlock(startPage > 1)
                .hasNextBlock(endPage < totalPages)
                .build();
    }

    /** 등록 (ADMIN / MANAGER만) */
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @PostMapping
    public ResponseEntity<?> create(@RequestBody ItemDTO dto) {
        int r = itemService.addItem(dto);
        if (r != 1) return ResponseEntity.badRequest().body("등록 실패");
        return ResponseEntity.created(URI.create("/api/items/" + dto.getItemNo())).body(dto);
    }

    /** 수정 (ADMIN / MANAGER만) */
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @PutMapping("/{itemNo}")
    public ResponseEntity<?> update(@PathVariable Long itemNo, @RequestBody ItemDTO dto) {
        dto.setItemNo(itemNo);
        int r = itemService.setItem(dto);
        return (r == 1) ? ResponseEntity.ok(dto) : ResponseEntity.badRequest().body("수정 실패");
    }

    /** 삭제(소프트) (ADMIN / MANAGER만) */
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @DeleteMapping("/{itemNo}")
    public ResponseEntity<?> delete(@PathVariable Long itemNo) {
        int r = itemService.removeItem(itemNo);
        return (r == 1) ? ResponseEntity.ok().build() : ResponseEntity.badRequest().body("삭제 실패");
    }
}
