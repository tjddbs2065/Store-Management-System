package com.erp.service;

import com.erp.exception.StoreItemLimitConflictException;
import com.erp.exception.StoreItemNotFoundException;
import com.erp.controller.request.SearchRequestDTO;
import com.erp.dto.PageResponseDTO;
import com.erp.dto.StoreItemDTO;
import com.erp.repository.StoreItemRepository;
import com.erp.repository.entity.StoreItem;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
@Rollback
class StoreItemServiceTest {

    @Autowired
    StoreItemService storeItemService;

    @Autowired
    StoreItemRepository storeItemRepository;

    // ==========================
    // 1) 재고 조회(getStoreItems)
    // ==========================

    @Test
    void getStoreItems_basic() {
        // given
        SearchRequestDTO req = new SearchRequestDTO();
        req.setStoreNo(1L);    // 샘플 데이터 기준 (1호점)
        req.setPage(0);
        req.setSize(10);

        // when
        PageResponseDTO<StoreItemDTO> res = storeItemService.getStoreItems(req);

        // then
        System.out.println("=== getStoreItems_basic ===");
        System.out.println("page         = " + res.getPage());
        System.out.println("totalElements= " + res.getTotalElements());
        System.out.println("totalPages   = " + res.getTotalPages());
        res.getContent().forEach(row -> System.out.printf(
                "store=%s | code=%s | name=%s | category=%s | finalLimit=%s | current=%s %s | owner=%s%n",
                row.getStoreName(),
                row.getItemCode(),
                row.getItemName(),
                row.getItemCategory(),
                row.getFinalLimit(),
                row.getCurrentQuantity(),
                row.getStockUnit(),
                row.getLimitOwner()
        ));
        System.out.println("========================================");

        assertThat(res).isNotNull();
        assertThat(res.getPage()).isEqualTo(0);   // 0-base
    }

    /**
     * 카테고리 + 품목명 검색
     */
    @Test
    void getStoreItems_withCategoryAndItemName() {

        SearchRequestDTO req = new SearchRequestDTO();
        req.setStoreNo(1L);
        req.setCategory("도우");
        req.setSearchType("NAME");
        req.setKeyword("도우");
        req.setPage(0);
        req.setSize(20);

        PageResponseDTO<StoreItemDTO> res = storeItemService.getStoreItems(req);

        System.out.println("=== 카테고리+품목명 검색 결과 ===");
        res.getContent().forEach(item -> System.out.printf(
                "store=%s | code=%s | name=%s | category=%s%n",
                item.getStoreName(),
                item.getItemCode(),
                item.getItemName(),
                item.getItemCategory()
        ));
        System.out.println("totalElements = " + res.getTotalElements());
        System.out.println("=======================================");

        res.getContent().forEach(item -> {
            assertThat(item.getItemCategory()).isEqualTo("도우");
            assertThat(item.getItemName()).contains("도우");
        });
    }

    /**
     * 카테고리 + 품목코드 검색
     */
    @Test
    void getStoreItems_withCategoryAndItemCode() {

        SearchRequestDTO req = new SearchRequestDTO();
        req.setStoreNo(1L);
        req.setCategory("도우");
        req.setSearchType("CODE");
        req.setKeyword("DOUGH");
        req.setPage(0);
        req.setSize(20);

        PageResponseDTO<StoreItemDTO> res = storeItemService.getStoreItems(req);

        System.out.println("=== 카테고리+품목코드 검색 결과 ===");
        res.getContent().forEach(item -> System.out.printf(
                "store=%s | code=%s | name=%s | category=%s%n",
                item.getStoreName(),
                item.getItemCode(),
                item.getItemName(),
                item.getItemCategory()
        ));
        System.out.println("totalElements = " + res.getTotalElements());
        System.out.println("=======================================");

        res.getContent().forEach(item -> {
            assertThat(item.getItemCategory()).isEqualTo("도우");
            assertThat(item.getItemCode()).contains("DOUGH");
        });
    }

    // ==========================
    // 2) 하한선 설정(setStoreItemLimit)
    // ==========================

    @Test
    void setStoreItemLimit_byManager() {

        Page<StoreItem> page = storeItemRepository.findAll(PageRequest.of(0, 1));
        assertThat(page.getTotalElements()).isGreaterThan(0L);

        StoreItem item = page.getContent().get(0);
        Long storeItemNo = item.getStoreItemNo();

        System.out.println("[setStoreItemLimit_byManager] 테스트 대상 storeItemNo = " + storeItemNo);

        // when
        storeItemService.setStoreItemLimit(storeItemNo, 50, true);

        // then
        StoreItem reloaded = storeItemRepository.findById(storeItemNo)
                .orElseThrow();

        System.out.println("managerLimit = " + reloaded.getManagerLimit()
                + ", storeLimit = " + reloaded.getStoreLimit());

        assertThat(reloaded.getManagerLimit()).isEqualTo(50);
    }

    @Test
    void setStoreItemLimit_conflictWithManagerLimit() {

        Page<StoreItem> page = storeItemRepository.findAll(PageRequest.of(0, 1));
        assertThat(page.getTotalElements()).isGreaterThan(0L);

        StoreItem item = page.getContent().get(0);
        Long storeItemNo = item.getStoreItemNo();

        // 먼저 본사에서 하한선 100 설정
        storeItemService.setStoreItemLimit(storeItemNo, 100, true);

        // 직영점이 수정 시도 → 예외
        StoreItemLimitConflictException ex = assertThrows(
                StoreItemLimitConflictException.class,
                () -> storeItemService.setStoreItemLimit(storeItemNo, 30, false)
        );

        System.out.println("[setStoreItemLimit_conflictWithManagerLimit] 메시지 = " + ex.getMessage());

        // 다시 조회해서 본사 하한선이 그대로인지 확인
        StoreItem reloaded = storeItemRepository.findById(storeItemNo).orElseThrow();
        assertThat(reloaded.getManagerLimit()).isEqualTo(100);
    }

    @Test
    void setStoreItemLimit_notFound() {
        long notExistingId = 999_999_999L;

        StoreItemNotFoundException ex = assertThrows(
                StoreItemNotFoundException.class,
                () -> storeItemService.setStoreItemLimit(notExistingId, 10, true)
        );

        System.out.println("[setStoreItemLimit_notFound] 메시지 = " + ex.getMessage());
        assertThat(ex.getMessage()).contains("존재하지 않는 재고 품목");
    }

    @Test
    void clearManagerLimit_withNull() {

        Page<StoreItem> page = storeItemRepository.findAll(PageRequest.of(0, 1));
        assertThat(page.getTotalElements()).isGreaterThan(0L);

        StoreItem item = page.getContent().get(0);
        Long storeItemNo = item.getStoreItemNo();

        // 본사 하한선 80 설정
        storeItemService.setStoreItemLimit(storeItemNo, 80, true);
        // 본사 하한선 제거
        storeItemService.setStoreItemLimit(storeItemNo, null, true);

        StoreItem reloaded = storeItemRepository.findById(storeItemNo).orElseThrow();

        System.out.println("[clearManagerLimit_withNull] managerLimit=" + reloaded.getManagerLimit()
                + ", storeLimit=" + reloaded.getStoreLimit());

        assertThat(reloaded.getManagerLimit()).isNull();
    }

    @Test
    void clearStoreLimit_withNull_whenNoManagerLimit() {

        Page<StoreItem> page = storeItemRepository.findAll(PageRequest.of(0, 1));
        assertThat(page.getTotalElements()).isGreaterThan(0L);

        StoreItem item = page.getContent().get(0);
        Long storeItemNo = item.getStoreItemNo();

        // 1) 본사 하한선 확실히 제거
        storeItemService.setStoreItemLimit(storeItemNo, null, true);

        // 2) 직영점 하한선 30 설정
        storeItemService.setStoreItemLimit(storeItemNo, 30, false);

        // 3) 직영점 하한선 제거(null)
        storeItemService.setStoreItemLimit(storeItemNo, null, false);

        StoreItem reloaded = storeItemRepository.findById(storeItemNo).orElseThrow();

        System.out.println("[clearStoreLimit_withNull_whenNoManagerLimit] managerLimit=" + reloaded.getManagerLimit()
                + ", storeLimit=" + reloaded.getStoreLimit());

        assertThat(reloaded.getManagerLimit()).isNull();
        assertThat(reloaded.getStoreLimit()).isNull();
    }
}
