package com.erp.controller;

import com.erp.dto.MenuDTO;
import com.erp.service.MenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.stream.Collectors;


@Controller
@RequiredArgsConstructor
@RequestMapping("menu")
public class MenuController {
    private final MenuService menuService;

    @GetMapping("menuSet")
    private String menuSet(@RequestParam Long menuNo, Model model){

        MenuDTO menu = menuService.getMenuDetail(menuNo);

        String menuNos = menu.getSizeList().stream()
                .map(s -> String.valueOf(s.getMenuNo()))
                .collect(Collectors.joining(","));

        model.addAttribute("menuNos", menuNos);
        model.addAttribute("menu", menu);
        model.addAttribute("sizeList", menu.getSizeList());
        model.addAttribute("ingredients", menu.getIngredients());
        model.addAttribute("hasSize", menu.isHasSize());

        return "menu/menuSetUI";
    }

    @GetMapping("/menuUI")
    private String member(){
        return "menu/menuUI";
    }

    @GetMapping("/menuDetailUI")
    public String menuDetail(@RequestParam Long menuNo, Model model) {

        MenuDTO menu = menuService.getMenuDetail(menuNo);

        String menuNos = menu.getSizeList().stream()
                .map(s -> String.valueOf(s.getMenuNo()))
                .collect(Collectors.joining(","));

        model.addAttribute("menuNos", menuNos);
        model.addAttribute("menu", menu);
        model.addAttribute("sizeList", menu.getSizeList());
        model.addAttribute("ingredients", menu.getIngredients());
        model.addAttribute("hasSize", menu.isHasSize());

        return "menu/menuDetailUI";
    }

    @GetMapping("/menuAddUI")
    public String menuAdd() {
        return "menu/menuAddUI";
    }
}
