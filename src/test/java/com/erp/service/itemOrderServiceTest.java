package com.erp.service;

import com.erp.repository.entity.ItemOrder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;

@SpringBootTest
public class itemOrderServiceTest {
    @Autowired
    ItemOrderService orderService;

    @Test
    public void getItemOrderListTest() {
        System.out.println(orderService.getItemOrderList(1));
    }
    @Test
    public void getItemOrderListByDateTest() {
        System.out.println(orderService.getItemOrderListByDate(0, LocalDate.of(2025, 11, 10), LocalDate.of(2025, 11, 20)));
    }
    @Test
    public void getItemsOrderByDayTest() {
        System.out.println(orderService.getItemOrderListByDay(0, 1));
    }

    @Test
    public void getItemsOrderByStatus() {
        System.out.println(orderService.getItemOrderListByDay(0, 1));
    }

    @Test
    public void getItemOrderListByStoreNoTest() {
        System.out.println(orderService.getItemOrderListByStore(0, 1L));
    }

    @Test
    public void getItemOrderDetailListTestByItemOrderNoTest() {
        System.out.println(orderService.getItemOrderDetailByOrderNo(1L));
    }

    @Test
    public void itemList(){
        System.out.println(orderService.itemList(1L));
    }
}
