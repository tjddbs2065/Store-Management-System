package com.erp.dao;

import com.erp.dto.MenuDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class MenuTest {
    @Autowired
    private MenuDAO menuDAO;

    @Test
    void setMenuTest() {
        MenuDTO menu = MenuDTO.builder()
                .menuNo(20L)
                .menuName("갈릭 디핑 쏘스")
                .menuCode("SAUCE")
                .menuExplain("맛있는 소스입니다")
                .menuImage("https://picsum.photos/300/200")
                .menuPrice(500)
                .releaseStatus("출시 예정")
                .build();

        menuDAO.setMenu(menu);
    }

    @Test
    void addMenuTest() {
        MenuDTO menu = MenuDTO.builder()
                .menuName("띠드 피자")
                .menuCode("C_PIZZA")
                .menuCategory("피자")
                .menuExplain("맛있는 피자입니다")
                .size("단일")
                .menuImage("https://picsum.photos/300/200")
                .menuPrice(25000)
                .releaseStatus("출시 예정")
                .build();
        menuDAO.addMenu(menu);
    }

    @Test
    void removeMenuTest() {
        menuDAO.removeMenu(23L);
    }

    @Test
    void getAllMenuTest() {
        List<MenuDTO> menuList = menuDAO.getMenuList(null, null);
        System.out.println(menuList);
    }

    @Test
    void getMenuByCategoryTest() {
        List<MenuDTO> menuList = menuDAO.getMenuList("피자", null);
        System.out.println(menuList);
    }

    @Test
    void getMenuByReleaseStatusTest() {
        List<MenuDTO> menuList = menuDAO.getMenuList(null, "출시 예정");
        System.out.println(menuList);
    }
}
