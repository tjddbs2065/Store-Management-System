package com.erp.dao;

import com.erp.dto.ItemDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class ItemDAOTest {

    @Autowired
    private ItemDAO itemDAO;

    // 1) 새 품목 등록
    @Test
    void addItem() {

        ItemDTO item = ItemDTO.builder()
                .itemCode("TEST-001")
                .itemCategory("테스트카테고리")
                .itemName("테스트아이템")
                .ingredientName("테스트재료명")
                .stockUnit("ea")
                .supplyUnit("box")
                .convertStock(100)
                .itemPrice(12345)
                .supplier("테스트공급사")
                .storageType("냉장")
                .expirationType("입고 후 n일")
                .expiration(3)
                .itemImage("/img/test.png")
                .note("JUnit 등록 테스트")
                .build();

        int result = itemDAO.addItem(item);

        System.out.println("▶ addItem result = " + result + ", PK = " + item.getItemNo());
        assertEquals(1, result);
        assertNotNull(item.getItemNo()); // useGeneratedKeys 확인
    }

    // 2) 품목 수정
    @Test
    void setItem() {
        List<ItemDTO> list = itemDAO.getByItemCode("TEST-001");
        assertFalse(list.isEmpty());

        ItemDTO item = list.get(0);
        Long itemNo = item.getItemNo();
        assertNotNull(itemNo);

        item.setItemCategory("수정된카테고리");
        item.setItemCode("EDIT-001");
        item.setItemName("수정된 품목명");
        item.setIngredientName("수정된 재료명");
        item.setStockUnit("g");
        item.setSupplyUnit("box");
        item.setConvertStock(777);
        item.setItemPrice(99999);
        item.setSupplier("수정된 공급사");
        item.setStorageType("냉동");
        item.setExpirationType("제조일자 기준");
        item.setExpiration(30);
        item.setItemImage("/img/edited.png");
        item.setNote("수정된 테스트 비고");

        int result = itemDAO.setItem(item);
        assertEquals(1, result);

        ItemDTO updated = itemDAO.getItemDetail(itemNo);
        assertEquals("EDIT-001", updated.getItemCode());
        assertEquals("수정된 품목명", updated.getItemName());

        System.out.println("▶ 수정된 item = " + updated);
    }

    // 3) 전체 목록 조회
//    @Test
//    void getItemList() {
//        List<ItemDTO> list = itemDAO.getItemList();
//
//        System.out.println("▶ getItemList size = " + list.size());
//        list.forEach(System.out::println);
//
//        assertNotNull(list);
//        assertFalse(list.isEmpty());
//    }

    // 4) 카테고리 검색
    @Test
    void getByCategory() {
        List<ItemDTO> list = itemDAO.getByCategory("도우");

        System.out.println("▶ getByCategory(도우) size = " + list.size());
        list.forEach(System.out::println);

        assertNotNull(list);
    }

    // 5) 품목명 검색
    @Test
    void getByItemName() {
        List<ItemDTO> list = itemDAO.getByItemName("도우");

        System.out.println("▶ getByItemName size = " + list.size());
        list.forEach(System.out::println);

        assertNotNull(list);
    }

    // 6) 품목코드 검색
    @Test
    void getByItemCode() {
        List<ItemDTO> list = itemDAO.getByItemCode("DOUGH");

        System.out.println("▶ getByItemCode size = " + list.size());
        list.forEach(System.out::println);

        assertNotNull(list);
    }

    // 7) 재료명 검색
    @Test
    void getByIngredient() {
        List<ItemDTO> list = itemDAO.getByIngredient("페퍼로니");

        System.out.println("▶ getByIngredient size = " + list.size());
        list.forEach(System.out::println);

        assertNotNull(list);
    }

    // 8) 품목 상세 보기
    @Test
    void getItemDetail() {
        ItemDTO item = itemDAO.getItemDetail(1L); // 샘플데이터 1번 기준

        System.out.println("▶ getItemDetail = " + item);
        assertNotNull(item);
    }

    // 9) 품목 삭제 (소프트 삭제)
    @Test
    void removeItem() {
        List<ItemDTO> list = itemDAO.getByItemCode("EDIT-001");

        if (!list.isEmpty()) {
            Long itemNo = list.get(0).getItemNo();

            int result = itemDAO.removeItem(itemNo);
            System.out.println("▶ removeItem result = " + result);
            assertEquals(1, result);
        }
    }
}
