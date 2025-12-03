package com.erp.repository;

import com.erp.dto.SalesOrderDTO;
import com.erp.dto.StoreDailyMenuSalesDTO;
import com.erp.dto.StoreMenuSalesSummaryDTO;
import com.erp.repository.entity.SalesOrder;
import com.erp.repository.entity.StoreOrderDetail;
import org.apache.ibatis.annotations.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;


import java.time.LocalDateTime;
import java.util.List;

public interface StoreOrderDetailRepository extends JpaRepository<StoreOrderDetail, Long> {
    @Query("""
    SELECT new com.erp.dto.SalesOrderDTO(
       o.salesOrderNo,
       SUM(d.menuCount),
       SUM(d.menuCount * d.menuPrice)
   )
       FROM StoreOrderDetail d
       JOIN d.salesOrder o
       WHERE o.salesOrderNo = :salesOrderNo
       GROUP BY o.salesOrderNo
""")
    SalesOrderDTO countSalesOrder(@Param("salesOrderNo") Long salesOrderNo);

    @Query("""
        select new com.erp.dto.StoreDailyMenuSalesDTO(
            so.store.storeName,
            m.menuCategory,
            m.menuName,
            m.size,
            sod.menuCount,
            (sod.menuPrice * sod.menuCount)
        )
        from SalesOrder so
            join so.orderDetails sod
            join sod.storeMenu sm
            join sm.menu m
        where so.salesOrderDatetime >= :startDate
          and so.salesOrderDatetime < :endDate
        order by so.store.storeName asc,
                 (sod.menuPrice * sod.menuCount) desc
        """)
    List<StoreDailyMenuSalesDTO> findDailyMenuSales(@Param("startDate") LocalDateTime startDate, @Param("endDate")  LocalDateTime endDate);

    @Query("""
    select new com.erp.dto.StoreDailyMenuSalesDTO(
        so.store.storeName,
        m.menuCategory,
        m.menuName,
        m.size,
        sod.menuCount,
        (sod.menuPrice * sod.menuCount)
    )
    from SalesOrder so
        join so.orderDetails sod
        join sod.storeMenu sm
        join sm.menu m
    where so.store.storeNo = :storeNo       
      and so.salesOrderDatetime >= :start
      and so.salesOrderDatetime < :end
    order by (sod.menuPrice * sod.menuCount) desc
    """)
    List<StoreDailyMenuSalesDTO> findDailyMenuSalesByStore(@Param("storeNo") Long storeNo,
                                                           @Param("start") LocalDateTime start,
                                                           @Param("end") LocalDateTime end
    );

    @Query("""
        select d
        from StoreOrderDetail d
             JOIN FETCH d.salesOrder o
             JOIN FETCH o.store s
             JOIN FETCH d.storeMenu sm
             JOIN FETCH sm.menu m
        WHERE o.salesOrderNo = :salesOrderNo
""")
    List<StoreOrderDetail> getStoreOrderDetail(@Param("salesOrderNo") Long salesOrderNo);

    List<StoreOrderDetail> findBySalesOrder(SalesOrder salesOrder);


    @Query("""
    select new com.erp.dto.StoreMenuSalesSummaryDTO(
        so.store.storeName,
        m.menuCategory,
        m.menuName,
        m.size,
        SUM(sod.menuCount),
        SUM(sod.menuPrice * sod.menuCount)
    )
    from SalesOrder so
        join so.orderDetails sod
        join sod.storeMenu sm
        join sm.menu m
    where so.store.storeNo = :storeNo
      and so.salesOrderDatetime >= :start
      and so.salesOrderDatetime < :end
    group by so.store.storeName, m.menuCategory, m.menuName, m.size
    order by SUM(sod.menuPrice * sod.menuCount) desc
    """)
    List<StoreMenuSalesSummaryDTO> findDailyMenuSummary(
            @Param("storeNo") Long storeNo,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );
}
