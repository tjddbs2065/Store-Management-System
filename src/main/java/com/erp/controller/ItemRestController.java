package com.erp.controller;

import com.erp.dto.ItemDTO;
import com.erp.dto.PageResponseDTO;
import com.erp.service.ItemService;
import com.erp.awss3.S3Uploader;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/items")
public class ItemRestController {

    private final ItemService itemService;
    private final S3Uploader s3Uploader;


    /* -----------------------------------------
       1) 단건 조회
     ----------------------------------------- */
    @GetMapping("/{itemNo}")
    public ResponseEntity<ItemDTO> findOne(@PathVariable Long itemNo){
        ItemDTO dto = itemService.getDetail(itemNo);
        return (dto == null)
                ? ResponseEntity.notFound().build()
                : ResponseEntity.ok(dto);
    }


    /* -----------------------------------------
       2) 목록 + 검색 + 페이징
     ----------------------------------------- */
    @GetMapping("/list/{page}")
    public PageResponseDTO<ItemDTO> listPaged(
            @PathVariable int page,
            @RequestParam(required = false) String itemCategory,
            @RequestParam(required = false) String itemName,
            @RequestParam(required = false) String itemCode,
            @RequestParam(required = false) String ingredientName,
            @RequestParam(required = false) String supplier,
            @RequestParam(required = false, defaultValue = "10") Integer size
    ) {

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

        int pageSize = (size == null || size < 1) ? 10 : size;
        int totalElements = list.size();
        int totalPages = (totalElements == 0) ? 1 :
                (int) Math.ceil(totalElements / (double) pageSize);

        int currentPage = Math.max(1, Math.min(page, totalPages));
        int fromIndex = (currentPage - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, totalElements);

        List<ItemDTO> pageContent =
                (fromIndex >= totalElements) ? List.of() : list.subList(fromIndex, toIndex);

        int blockSize = 5;
        int blockIdx = (currentPage - 1) / blockSize;
        int startPage = blockIdx * blockSize + 1;
        int endPage = Math.min(totalPages, startPage + blockSize - 1);

        return PageResponseDTO.<ItemDTO>builder()
                .content(pageContent)
                .page(currentPage - 1)
                .size(pageSize)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .startPage(startPage)
                .endPage(endPage)
                .hasPrevBlock(startPage > 1)
                .hasNextBlock(endPage < totalPages)
                .build();
    }


    /* -----------------------------------------
       3) 품목 등록
     ----------------------------------------- */
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @PostMapping
    public ResponseEntity<?> create(@RequestBody ItemDTO dto) {

        int r = itemService.addItem(dto);
        if (r != 1) return ResponseEntity.badRequest().body("등록 실패");

        return ResponseEntity.created(URI.create("/api/items/" + dto.getItemNo())).body(dto);
    }


    /* -----------------------------------------
       4) 이미지 S3 업로드 (S3Uploader 사용)
     ----------------------------------------- */
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @PostMapping("/upload-image")
    public ResponseEntity<?> uploadImage(@RequestParam("file") MultipartFile file) {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("파일이 없습니다.");
        }

        try {
            String url = s3Uploader.uploadItemImage(file);
            return ResponseEntity.ok(url);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("업로드 실패");
        }
    }


    /* -----------------------------------------
       5) 수정
     ----------------------------------------- */
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @PutMapping("/{itemNo}")
    public ResponseEntity<?> update(
            @PathVariable Long itemNo,
            @RequestBody ItemDTO dto
    ) {
        dto.setItemNo(itemNo);
        int r = itemService.setItem(dto);

        return (r == 1)
                ? ResponseEntity.ok(dto)
                : ResponseEntity.badRequest().body("수정 실패");
    }


    /* -----------------------------------------
       6) 삭제 (소프트)
     ----------------------------------------- */
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @DeleteMapping("/{itemNo}")
    public ResponseEntity<?> delete(@PathVariable Long itemNo){
        int r = itemService.removeItem(itemNo);
        return (r == 1)
                ? ResponseEntity.ok().build()
                : ResponseEntity.badRequest().body("삭제 실패");
    }
}
