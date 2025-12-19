package com.erp.service;

import com.erp.exception.StoreItemLimitConflictException;
import com.erp.exception.StoreItemNotFoundException;
import com.erp.controller.request.SearchRequestDTO;
import com.erp.dto.PageResponseDTO;
import com.erp.dto.StoreItemDTO;
import com.erp.repository.StoreItemRepository;
import com.erp.repository.entity.StoreItem;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class StoreItemService {

    private final StoreItemRepository storeItemRepository;

    /**
     * 재고 조회 (본사/직영점 공용)
     */
    @Transactional(readOnly = true)
    public PageResponseDTO<StoreItemDTO> getStoreItems(SearchRequestDTO request) {


        // 1) 카테고리: "전체" 또는 빈값이면 null 처리 → 조건 무시
        String category = request.getCategory();
        if (!StringUtils.hasText(category) || "전체".equals(category)) {
            category = null;
        }

        // 2) 검색 타입 / 키워드 정리
        String searchType = request.getSearchType();
        String keyword = request.getKeyword();

        if (!StringUtils.hasText(keyword)) {
            keyword = null;
            searchType = null;
        } else {
            keyword = keyword.trim();
            if (!StringUtils.hasText(searchType)) {
                keyword = null;
                searchType = null;
            } else {
                searchType = searchType.toUpperCase(); // "NAME" / "CODE"
            }
        }

        // 3) 페이지/사이즈 기본값 (0-base page)
        int page = (request.getPage() == null || request.getPage() < 0)
                ? 0
                : request.getPage();

        int size = (request.getSize() == null || request.getSize() <= 0)
                ? 10
                : request.getSize();

        Pageable pageable = PageRequest.of(page, size);

        // 4) 레포지토리 호출 (DTO 프로젝션 + Page)
        Page<StoreItemDTO> result = storeItemRepository.searchStoreItems(
                request.getStoreNo(),
                category,
                searchType,
                keyword,
                pageable
        );

        // 5) 블럭 페이지네이션 계산
        int totalPages = result.getTotalPages();
        int blockSize = 10;
        int currentPage = result.getNumber() + 1;  // 1-base

        int startPage;
        int endPage;
        boolean hasPrevBlock;
        boolean hasNextBlock;

        if (totalPages == 0) {
            startPage = 0;
            endPage = 0;
            hasPrevBlock = false;
            hasNextBlock = false;
        } else {
            int blockIndex = (currentPage - 1) / blockSize;
            startPage = blockIndex * blockSize + 1;
            endPage = Math.min(startPage + blockSize - 1, totalPages);

            hasPrevBlock = startPage > 1;
            hasNextBlock = endPage < totalPages;
        }

        // 6) Page → PageResponseDTO 로 포장
        return PageResponseDTO.<StoreItemDTO>builder()
                .content(result.getContent())
                .page(result.getNumber())          // 0-base
                .size(result.getSize())
                .totalElements(result.getTotalElements())
                .totalPages(totalPages)
                .startPage(startPage)
                .endPage(endPage)
                .hasPrevBlock(hasPrevBlock)
                .hasNextBlock(hasNextBlock)
                .build();
    }


    @Transactional
    public void setStoreItemLimit(Long storeItemNo,
                                  Integer newLimit,
                                  boolean isManagerRole) {


        // 품목 조회
        StoreItem storeItem = storeItemRepository.findById(storeItemNo)
                .orElseThrow(() -> new StoreItemNotFoundException("직영점 품목 정보 조회 실패: " + storeItemNo));

        // 본사 하한선이 이미 있고, 직영점 계정이 수정하려고 하면 차단
        if (!isManagerRole && storeItem.getManagerLimit() != null) {
            throw new StoreItemLimitConflictException("본사에서 이미 하한선을 설정한 품목입니다. 직영점에서는 수정할 수 없습니다.");
        }

        // 역할에 따라 하한선 필드 분기
        if (isManagerRole) {
            // ROLE_MANAGER / ROLE_ADMIN → 본사 하한선
            storeItem.setManagerLimit(newLimit);   // null 이면 본사 하한선 제거
        } else {
            // ROLE_STORE → 직영점 하한선
            storeItem.setStoreLimit(newLimit);     // null 이면 직영점 하한선 제거
        }

        storeItemRepository.save(storeItem);
    }
}
