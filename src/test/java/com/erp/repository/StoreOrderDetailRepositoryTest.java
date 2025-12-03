package com.erp.repository;

import com.erp.dto.SalesOrderDTO;
import com.erp.repository.entity.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class StoreOrderDetailRepositoryTest {

    @Autowired
    private StoreOrderDetailRepository storeOrderDetailRepository;

    @Autowired
    private SalesOrderRepository salesOrderRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private StoreMenuRepository storeMenuRepository;

    @Autowired
    private StoreStockRepository storeStockRepository;

    @Autowired
    private StoreItemRepository storeItemRepository;

    @Autowired
    private MenuIngredientRepository menuIngredientRepository;

    @Test
    void findDailyMenuSalesTest(){
        System.out.println(storeOrderDetailRepository.findDailyMenuSales(LocalDateTime.of(2024,1,7,0,0   ),LocalDateTime.of(2024,1,8,0,0)));
    }
    @Test
    void findDailyMenuSalesByStore(){
        System.out.println(storeOrderDetailRepository.findDailyMenuSalesByStore(5L,LocalDateTime.of(2024,1,7,0,0   ),LocalDateTime.of(2024,1,8,0,0)));
    }


    @Test
    @Transactional
    @Rollback(false)
    void calculateOrderSummaryTest() {
        //샘플 넣은 거라서 코드가 깁니다..! service에선 예제샘플 넣을 필요 없으니 짧아질 듯..
        Store store = storeRepository.findById(2L)
                .orElseThrow(() -> new RuntimeException("Store not found"));
        SalesOrder order = new SalesOrder();
        order.setStore(store);
        order.setSalesOrderDatetime(LocalDateTime.now());
        order.setSalesOrderAmount(0);
        salesOrderRepository.save(order);

        Long orderNo = order.getSalesOrderNo();

        StoreMenu menu13 = storeMenuRepository.findById(13L)
                .orElseThrow(() -> new RuntimeException("Menu 13 not found"));

        StoreMenu menu14 = storeMenuRepository.findById(14L)
                .orElseThrow(() -> new RuntimeException("Menu 14 not found"));

        StoreOrderDetail detail1 = new StoreOrderDetail();
        detail1.setSalesOrder(order);
        detail1.setStoreMenu(menu13);
        detail1.setMenuCount(2);
        detail1.setMenuPrice(10000);
        storeOrderDetailRepository.save(detail1);

        StoreOrderDetail detail2 = new StoreOrderDetail();
        detail2.setSalesOrder(order);
        detail2.setStoreMenu(menu14);
        detail2.setMenuCount(1);
        detail2.setMenuPrice(8000);
        storeOrderDetailRepository.save(detail2);

        SalesOrderDTO summary = storeOrderDetailRepository.countSalesOrder(orderNo);
        int totalAmount = summary.getSalesOrderAmount();
        int totalMenu = summary.getSalesOrderCount();
        order.setSalesOrderAmount(totalAmount);
        salesOrderRepository.save(order);

        System.out.println("총 판매금액 = " + order.getSalesOrderAmount());
        System.out.println("총 메뉴개수 = " + totalMenu);

    }

    @Test
    @Transactional
    void getStoreOrderDetailTest() {
        Long salesOrderNo = 655L;
        List<StoreOrderDetail> details = storeOrderDetailRepository.getStoreOrderDetail(salesOrderNo);

        StoreOrderDetail d = details.get(0);

        System.out.println("===== [주문 정보] =====");
        System.out.println("주문번호 : " + d.getSalesOrder().getSalesOrderNo());
        System.out.println("주문일시 : " + d.getSalesOrder().getSalesOrderDatetime());
        System.out.println("매장명   : " + d.getSalesOrder().getStore().getStoreName());

        //나중에 dto로 묶어서 뷰 내보내면 됨
        System.out.println("\n===== [주문 상세 목록] =====");
        for (StoreOrderDetail detail : details) {
            System.out.println("상세번호 : " + detail.getStoreOrderDetailNo());
            System.out.println("메뉴명   : " + detail.getStoreMenu().getMenu().getMenuName());
            System.out.println("사이즈   : " + detail.getStoreMenu().getMenu().getSize());
            System.out.println("단가     : " + detail.getMenuPrice());
            System.out.println("수량     : " + detail.getMenuCount());
            System.out.println("총금액   : " + detail.getMenuPrice() * detail.getMenuCount());
            System.out.println("--------------------------------------");
        }
    }

    @Test
    @Transactional
    @Rollback(false)
    void addStoreOrderTest() {
        Store store = storeRepository.findById(1L).orElseThrow(() -> new RuntimeException("Store not found"));
        StoreMenu menu1 = storeMenuRepository.findById(5L).orElseThrow(() -> new RuntimeException("Menu 13 not found"));
        StoreMenu menu2 = storeMenuRepository.findById(6L).orElseThrow(() -> new RuntimeException("Menu 12 not found"));

        SalesOrder salesOrder = SalesOrder.builder()
                .store(store)
                .salesOrderDatetime(LocalDateTime.now())
                .salesOrderAmount(30000)
                .build();

        salesOrderRepository.save(salesOrder);

        // 메뉴1 - 2개 주문
        StoreOrderDetail detail1 = StoreOrderDetail.builder()
                .salesOrder(salesOrder)
                .storeMenu(menu1)
                .menuCount(2)
                .menuPrice(menu1.getMenu().getMenuPrice())  // menu entity 안에 price 필드라고 가정
                .build();
        storeOrderDetailRepository.save(detail1);

        // 메뉴2 - 1개 주문
        StoreOrderDetail detail2 = StoreOrderDetail.builder()
                .salesOrder(salesOrder)
                .storeMenu(menu2)
                .menuCount(1)
                .menuPrice(menu2.getMenu().getMenuPrice())
                .build();
        storeOrderDetailRepository.save(detail2);

        List<StoreOrderDetail> details =
                storeOrderDetailRepository.findBySalesOrder(salesOrder);


        for (StoreOrderDetail d : details) {

            StoreMenu orderedMenu = d.getStoreMenu();
            int menuCount = d.getMenuCount();


            // 메뉴 → 재료 목록 조회(menu_ingredient)
            List<MenuIngredient> ingredientList =
                    menuIngredientRepository.findByMenu_MenuNo(
                            orderedMenu.getMenu().getMenuNo()
                    );

            for (MenuIngredient ing : ingredientList) {

                Long itemNo = ing.getItem().getItemNo();
                int needQty = ing.getIngredientQuantity();
                int totalConsume = needQty * menuCount;


                // 🚨 너가 준 코드: 매장 품목 정보 획득
                StoreItem storeItem = storeItemRepository
                        .findByStoreNoAndItemNo(store.getStoreNo(), itemNo)
                        .stream()
                        .findFirst()
                        .orElseThrow(() ->
                                new RuntimeException("매장 보유 품목 없음: itemNo=" + itemNo));


                // 🚨 너가 준 코드: 현재 매장 품목 재고 최신값 획득
                StoreStock latestStock = storeStockRepository
                        .findFirstByStoreItemNoOrderByStoreStockNoDesc(
                                storeItem.getStoreItemNo()
                        );

                int previousQty = (latestStock == null) ? 0 : latestStock.getCurrentQuantity();
                int updatedQty = previousQty - totalConsume;


                // 재고 차감 로그 INSERT
                StoreStock newStock = StoreStock.builder()
                        .storeItemNo(storeItem.getStoreItemNo())
                        .changeQuantity(-totalConsume)
                        .currentQuantity(updatedQty)
                        .changeReason("판매")
                        .build();

                storeStockRepository.save(newStock);
            }
        }


        // =====================================================
        // 6) 로그 출력(검증)
        // =====================================================
        System.out.println("\n===== [주문 상세 확인] =====");
        storeOrderDetailRepository.findBySalesOrder(salesOrder)
                .forEach(d -> System.out.println(
                        d.getStoreMenu().getMenu().getMenuName() +
                                ", 수량=" + d.getMenuCount() +
                                ", 가격=" + d.getMenuPrice()
                ));

        System.out.println("\n===== [재고 변경 로그] =====");
        storeStockRepository.findAll()
                .forEach(s -> System.out.println(
                        "[storeItemNo=" + s.getStoreItemNo() +
                                "] change=" + s.getChangeQuantity() +
                                ", current=" + s.getCurrentQuantity() +
                                ", time=" + s.getChangeDatetime()
                ));
    }
}
