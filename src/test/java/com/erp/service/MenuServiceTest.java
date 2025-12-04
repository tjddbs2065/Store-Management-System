package com.erp.service;

import com.erp.repository.entity.StoreMenu;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class MenuServiceTest {
    @Autowired
    MenuService menuService;
    @Autowired
    StoreMenuService storeMenuService;

    @Test
    void searchMenuTest(){
        System.out.println(storeMenuService.searchMenu("피자",null,null));

    }


    @Test
    public void getMenuListTest(){
        System.out.println(menuService.getMenuList(null, null));
    }

    @Test
    public void getMenuListByCategoryTest(){
        System.out.println(menuService.getMenuList("피자", null));
    }

    @Test
    public void getMenuListByReleaseStatusTest(){
        System.out.println(menuService.getMenuList(null, "출시 예정"));
    }


    @Test
    public void getMenuDetailTest(){
        Long menuNo = 1L;
        System.out.println(menuService.getMenuDetail(menuNo));
    }
}
