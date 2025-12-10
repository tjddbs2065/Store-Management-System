package com.erp.controller;

import com.erp.dto.ItemDTO;
import com.erp.dto.MenuDTO;
import com.erp.service.ItemService;
import com.erp.service.MenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/menu")
public class MenuRestController {
    private final MenuService menuService;
    private final ItemService itemService;

    @DeleteMapping("/delMenu")
    public ResponseEntity<Map<String, String>> removeMenu(@RequestParam Long menuNo) {
        menuService.removeMenu(menuNo);
        return ResponseEntity.ok(Map.of("message","delete Menu success"));
    }

    @PostMapping("/setMenu")
    public ResponseEntity<Map<String, String>> setMenu(@RequestBody MenuDTO menuDTO) {
        menuService.updateMenu(menuDTO);
        return ResponseEntity.ok(Map.of("message", "set Menu success"));
    }

    @GetMapping("/menuList")
    public ResponseEntity<List<MenuDTO>> getMenuList(
            @RequestParam(required = false) String menuCategory,
            @RequestParam(required = false) String releaseStatus
    ) {
        List<MenuDTO> menuList = menuService.getMenuList(menuCategory, releaseStatus);
        return ResponseEntity.ok(menuList);
    }

    @PostMapping("/addMenu")
    public ResponseEntity<?> addMenu(
            @RequestBody MenuDTO menuDTO
    ){
        menuService.addMenu(menuDTO);
        return ResponseEntity.ok().body(Map.of("message", "Request addSalesOrder success"));
    }

    @GetMapping("/itemList")
    public ResponseEntity<List<ItemDTO>> getItemList(
            @RequestParam(required = false) String itemCategory,
            @RequestParam(required = false) String ingredientName,
            @RequestParam(required = false) String itemCode
    ) {
        List<ItemDTO> itemList = itemService.getItemList(itemCategory, ingredientName, itemCode);
        return ResponseEntity.ok(itemList);
    }
}
