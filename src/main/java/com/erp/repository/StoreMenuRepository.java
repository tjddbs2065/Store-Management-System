package com.erp.repository;

import com.erp.dto.StoreMenuDTO;
import com.erp.repository.entity.StoreMenu;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface StoreMenuRepository extends JpaRepository<StoreMenu, Long> {

        void deleteByMenu_MenuNo(Long menuNo);
        boolean existsByStore_StoreNoAndMenu_MenuNo(Long storeNo, Long menuNo);

        @Modifying
        @Transactional
        @Query("""
            UPDATE StoreMenu sm 
            SET sm.salesStatus = :status
            WHERE sm.storeMenuNo = :storeMenuNo
    """)
        int setSalesStatus(@Param("storeMenuNo") Long storeMenuNo,
                           @Param("status") String salesStatus);



        @Query(
                value = """
        SELECT new com.erp.dto.StoreMenuDTO(
            sm.storeMenuNo,
            s.storeName,
            m.menuCode,
            m.menuName,
            m.size,
            m.menuPrice,
            sm.salesStatus,
            m.menuCategory           
        )
        FROM StoreMenu sm
            JOIN sm.store s
            JOIN sm.menu m
        WHERE
            (:storeName IS NULL OR s.storeName = :storeName)
            AND (:menuName IS NULL OR m.menuName LIKE %:menuName%)
            AND m.releaseStatus = '출시 중'
            AND m.delDate IS NULL
            AND (:salesStatus IS NULL OR sm.salesStatus = :salesStatus)
            AND (:menuCategory IS NULL OR m.menuCategory = :menuCategory)
    """,
                countQuery = """
        SELECT COUNT(sm.storeMenuNo)
        FROM StoreMenu sm
            JOIN sm.store s
            JOIN sm.menu m
        WHERE
            (:storeName IS NULL OR s.storeName = :storeName)
            AND (:menuName IS NULL OR m.menuName LIKE %:menuName%)
            AND m.releaseStatus = '출시 중'
            AND m.delDate IS NULL
            AND (:salesStatus IS NULL OR sm.salesStatus = :salesStatus)
            AND (:menuCategory IS NULL OR m.menuCategory = :menuCategory)
    """
        )
        Page<StoreMenuDTO> findStoreMenu(
                @Param("storeName") String storeName,
                @Param("menuName") String menuName,
                @Param("salesStatus") String salesStatus,
                @Param("menuCategory") String menuCategory,
                Pageable pageable
        );


        @Query(
                value = """
        SELECT new com.erp.dto.StoreMenuDTO(
            sm.storeMenuNo,
            s.storeName,
            m.menuCode,
            m.menuName,
            m.size,
            m.menuPrice,
            sm.salesStatus,
            m.menuCategory         
        )
        FROM StoreMenu sm
            JOIN sm.store s
            JOIN sm.menu m
        WHERE
            s.storeNo = :storeNo
            AND m.releaseStatus = '출시 중'
            AND m.delDate IS NULL
            AND (:menuName IS NULL OR m.menuName LIKE %:menuName%)
            AND (:salesStatus IS NULL OR sm.salesStatus = :salesStatus)
            AND (:menuCategory IS NULL OR m.menuCategory = :menuCategory)
    """,
                countQuery = """
        SELECT COUNT(sm.storeMenuNo)
        FROM StoreMenu sm
            JOIN sm.store s
            JOIN sm.menu m
        WHERE
            s.storeNo = :storeNo
            AND m.releaseStatus = '출시 중'
            AND m.delDate IS NULL
            AND (:menuName IS NULL OR m.menuName LIKE %:menuName%)
            AND (:salesStatus IS NULL OR sm.salesStatus = :salesStatus)
            AND (:menuCategory IS NULL OR m.menuCategory = :menuCategory)
    """
        )
        Page<StoreMenuDTO> findStoreMenuForStore(
                @Param("storeNo") Long storeNo,
                @Param("menuName") String menuName,
                @Param("salesStatus") String salesStatus,
                @Param("menuCategory") String menuCategory,
                Pageable pageable
        );


        @Query("""
         SELECT new com.erp.dto.StoreMenuDTO(
                sm.storeMenuNo,
                s.storeName,
                m.menuCode,
                m.menuName,
                m.size,
                m.menuPrice,
                sm.salesStatus,
                m.menuCategory      
            )
            FROM StoreMenu sm
                JOIN sm.store s
                JOIN sm.menu m
            WHERE
                s.storeNo = :storeNo
                AND sm.salesStatus = '판매중'
                AND m.releaseStatus = '출시 중'
                AND m.delDate IS NULL
        """)
        List<StoreMenuDTO> findStoreMenuByStoreNo(
                @Param("storeNo") Long storeNo
        );
}
