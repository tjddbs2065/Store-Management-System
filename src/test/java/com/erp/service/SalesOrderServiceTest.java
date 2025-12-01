//package com.erp.service;
//
//import com.erp.dto.SalesOrderDetailDTO;
//import com.erp.dto.StoreMenuDTO;
//import com.erp.repository.*;
//import com.erp.repository.entity.MenuIngredient;
//import com.erp.repository.entity.StoreItem;
//import com.erp.repository.entity.StoreMenu;
//import com.erp.repository.entity.StoreStock;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//
//import java.time.LocalDate;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//@SpringBootTest
//public class SalesOrderServiceTest {
//    @Autowired
//    SalesOrderService salesOrderService;
//
//    @Autowired
//    StoreMenuRepository storeMenuRepository;
//
//    @Autowired
//    StoreItemRepository storeItemRepository;
//
//    @Autowired
//    MenuIngredientRepository menuIngredientRepository;
//
//    @Autowired
//    StoreStockRepository storeStockRepository;
//    @Autowired
//    private StoreRepository storeRepository;
//
//
//    @Test
//    public void addSalesOrderTest() {
//
//        Long storeNo = 1L;
//
//        StoreMenu sm1 = storeMenuRepository.findById(5L)
//                .orElseThrow();  // 불고기 피자 라지 (28000)
//        StoreMenu sm2 = storeMenuRepository.findById(10L)
//                .orElseThrow();  // 코카콜라 1.25L (2000)
//
//        StoreMenuDTO m1 = new StoreMenuDTO();
//        m1.setStoreMenuNo(sm1.getStoreMenuNo());
//        m1.setMenuName(sm1.getMenu().getMenuName());
//        m1.setMenuCode(sm1.getMenu().getMenuCode());
//        m1.setMenuPrice(sm1.getMenu().getMenuPrice());
//        m1.setSize(sm1.getMenu().getSize());
//        m1.setSalesStatus(sm1.getSalesStatus());
//
//        StoreMenuDTO m2 = new StoreMenuDTO();
//        m2.setStoreMenuNo(sm2.getStoreMenuNo());
//        m2.setMenuName(sm2.getMenu().getMenuName());
//        m2.setMenuCode(sm2.getMenu().getMenuCode());
//        m2.setMenuPrice(sm2.getMenu().getMenuPrice());
//        m2.setSize(sm2.getMenu().getSize());
//        m2.setSalesStatus(sm2.getSalesStatus());
//
//        List<StoreMenuDTO> menuList = List.of(m1, m2);
//
//        int count1 = 2; // 불고기 2판
//        int count2 = 1; // 코카콜라 1개
//
//        SalesOrderDetailDTO d1 = new SalesOrderDetailDTO();
//        d1.setPrice(Integer.parseInt(m1.getMenuPrice()));
//        d1.setCount(count1);
//
//        SalesOrderDetailDTO d2 = new SalesOrderDetailDTO();
//        d2.setPrice(Integer.parseInt(m2.getMenuPrice()));
//        d2.setCount(count2);
//
//        List<SalesOrderDetailDTO> detailList = List.of(d1, d2);
//
//
//        Map<Long, Integer> beforeStockMap = new HashMap<>();
//
//        for (StoreMenuDTO dto : menuList) {
//            StoreMenu sm = storeMenuRepository.findById(dto.getStoreMenuNo()).orElseThrow();
//            List<MenuIngredient> ingredients = menuIngredientRepository.findByMenu_MenuNo(sm.getMenu().getMenuNo());
//
//            for (MenuIngredient ingredient : ingredients) {
//                Long itemNo = ingredient.getItem().getItemNo();
//
//                StoreItem storeItem = storeItemRepository.findByStoreNoAndItemNo(storeNo, itemNo).stream().findFirst().orElse(null);
//                if (storeItem == null) {
//                    StoreStock latest = storeStockRepository.findFirstByStoreItemNoOrderByStoreStockNoDesc(storeItem.getItemNo());
//
//                    beforeStockMap.put(storeItem.getStoreItemNo(), latest==null?0:latest.getCurrentQuantity());
//                }
//            }
//        }
//        salesOrderService.addSalesOrder(storeNo, menuList, detailList);
//
//        Map<Long, Integer> afterStockMap = new HashMap<>();
//
//        for (StoreMenuDTO menuDTO : menuList) {
//
//            StoreMenu sm = storeMenuRepository.findById(menuDTO.getStoreMenuNo()).orElseThrow();
//
//            List<MenuIngredient> ingredients = menuIngredientRepository
//                    .findByMenu_MenuNo(sm.getMenu().getMenuNo());
//
//            for (MenuIngredient ing : ingredients) {
//                Long itemNo = ing.getItem().getItemNo();
//
//                StoreItem storeItem = storeItemRepository
//                        .findByStoreNoAndItemNo(storeNo, itemNo)
//                        .stream().findFirst().orElse(null);
//
//                if (storeItem != null) {
//                    StoreStock latest = storeStockRepository
//                            .findFirstByStoreItemNoOrderByStoreStockNoDesc(storeItem.getStoreItemNo());
//
//                    afterStockMap.put(storeItem.getStoreItemNo(),
//                            latest == null ? 0 : latest.getCurrentQuantity());
//                }
//            }
//        }
//    }
//    @Test
//    public void getSalesOrderTest() {
//        System.out.println(salesOrderService.getSalesOrderList(1, null,null));
//    }
//
//    @Test
//    public void getSalesOrderDetailTest() {
//        System.out.println(salesOrderService.getSalesOrderDetail(655L));
//    }
//}
