package com.erp.repository;

import com.erp.dto.StoreItemDTO;
import com.erp.repository.entity.StoreItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StoreItemRepository extends JpaRepository<StoreItem, Long> {

    // 다른 기능(입고/폐기 등)에서 쓸 수 있는 기본 finder
    List<StoreItem> findByStoreNo(Long storeNo);

    List<StoreItem> findByStoreNoAndItemNo(Long storeNo, Long itemNo);
    @Query(
            value = """
SELECT DISTINCT new com.erp.dto.StoreItemDTO(
  si.storeItemNo,
  s.storeNo,
  s.storeName,
  i.itemNo,
  i.itemCode,
  i.itemName,
  i.itemCategory,
  COALESCE(si.managerLimit, si.storeLimit),
  CASE WHEN si.managerLimit IS NOT NULL THEN 'MANAGER'
       WHEN si.storeLimit   IS NOT NULL THEN 'STORE'
       ELSE 'NONE' END,
  COALESCE(ss.currentQuantity, 0),
  i.stockUnit,
  i.convertStock,
  si.managerLimit,
  si.storeLimit
)
FROM StoreItem si
JOIN Store s ON s.storeNo = si.storeNo
JOIN Item  i ON i.itemNo  = si.itemNo
LEFT JOIN StoreStock ss ON ss.storeStockNo = (
  SELECT MAX(ss2.storeStockNo)
  FROM StoreStock ss2
  WHERE ss2.storeItemNo = si.storeItemNo
)
JOIN MenuIngredient mi ON mi.item.itemNo = i.itemNo
JOIN mi.menu m
JOIN StoreMenu sm
  ON sm.menu = m
 AND sm.store.storeNo = :storeNo
 AND sm.salesStatus IN ('판매중','품절')
WHERE si.storeNo = :storeNo
  AND i.delDate IS NULL
  AND m.releaseStatus = '출시 중'
  AND m.delDate IS NULL
  AND (:category IS NULL OR i.itemCategory = :category)
  AND (
        :keyword IS NULL
     OR (:searchType = 'NAME' AND i.itemName LIKE CONCAT('%', :keyword, '%'))
     OR (:searchType = 'CODE' AND i.itemCode LIKE CONCAT('%', :keyword, '%'))
  )
ORDER BY i.itemCategory, i.itemName
""",
            countQuery = """
SELECT COUNT(DISTINCT si.storeItemNo)
FROM StoreItem si
JOIN Item  i ON i.itemNo  = si.itemNo
JOIN MenuIngredient mi ON mi.item.itemNo = i.itemNo
JOIN mi.menu m
JOIN StoreMenu sm
  ON sm.menu = m
 AND sm.store.storeNo = :storeNo
 AND sm.salesStatus IN ('판매중','품절')
WHERE si.storeNo = :storeNo
  AND i.delDate IS NULL
  AND m.releaseStatus = '출시 중'
  AND m.delDate IS NULL
  AND (:category IS NULL OR i.itemCategory = :category)
  AND (
        :keyword IS NULL
     OR (:searchType = 'NAME' AND i.itemName LIKE CONCAT('%', :keyword, '%'))
     OR (:searchType = 'CODE' AND i.itemCode LIKE CONCAT('%', :keyword, '%'))
  )
"""
    )
    Page<StoreItemDTO> searchStoreItems(@Param("storeNo") Long storeNo,
                                        @Param("category") String category,
                                        @Param("searchType") String searchType,
                                        @Param("keyword") String keyword,
                                        Pageable pageable);


}
