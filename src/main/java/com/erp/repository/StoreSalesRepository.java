package com.erp.repository;

import com.erp.dto.SalesListDTO;
import com.erp.repository.entity.StoreSales;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface StoreSalesRepository extends JpaRepository<StoreSales,Long> {

    Optional<StoreSales> findByStoreStoreNoAndSalesDate(Long storeNo, LocalDate salesDate);
    List<StoreSales> findBySalesDateBetween(LocalDate startDate, LocalDate endDate);
    List<StoreSales> findByStore_StoreNoAndSalesDateBetween(Long storeNo, LocalDate startDate, LocalDate endDate);
    @Query("""
        select new com.erp.dto.SalesListDTO(
            s.store.storeNo,
            s.store.storeName,
            s.store.address,
            (
                select count(o)
                from SalesOrder o
                where o.store.storeNo = s.store.storeNo
                  and function('DATE', o.salesOrderDatetime) = s.salesDate
            ),
            s.salesPrice,
            s.salesDate,
            null
        )
        from StoreSales s
        where (:startDate is null or s.salesDate >= :startDate)
          and (:endDate is null or s.salesDate <= :endDate)
          and (:storeName is null or s.store.storeName like concat('%', :storeName, '%'))
        order by s.salesDate desc
        """)
    Page<SalesListDTO> findSalesList(@Param("startDate") LocalDate startDate,
                                     @Param("endDate") LocalDate endDate,
                                     @Param("storeName") String storeName,
                                     Pageable pageable);
    @Query("""
    select new com.erp.dto.SalesListDTO(
        s.store.storeNo,
        s.store.storeName,
        s.store.address,
        (
            select count(o)
            from SalesOrder o
            where o.store.storeNo = s.store.storeNo
              and function('DATE', o.salesOrderDatetime) = s.salesDate
        ),
        s.salesPrice,
        s.salesDate,
        null
    )
    from StoreSales s
    where s.store.storeNo = :storeNo         
      and (:startDate is null or s.salesDate >= :startDate)
      and (:endDate is null or s.salesDate <= :endDate)
    order by s.salesDate desc
    """)
    Page<SalesListDTO> findSalesListForStore(
            @Param("storeNo") Long storeNo,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable
    );



}
