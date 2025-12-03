package com.erp.repository;

import com.erp.repository.entity.SalesOrder;
import org.apache.ibatis.annotations.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface SalesOrderRepository extends JpaRepository<SalesOrder, Long>, JpaSpecificationExecutor<SalesOrder> {


    @Query("""
    SELECT SUM(sod.menuCount)
    FROM StoreOrderDetail sod
    JOIN sod.salesOrder so
    WHERE so.salesOrderDatetime BETWEEN :start AND :end
""")
    Integer getTotalMenuCount(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );
//
//    @Query("""
//           SELECT COUNT(o)
//           FROM SalesOrder o
//           WHERE o.store.storeNo = :storeNo
//             AND DATE(o.salesOrderDatetime) = :salesOrderDate
//           """)
//    int countOrders(@Param("storeNo") Long storeNo, @Param("salesOrderDate") Date salesOrderDate );
//
//
//    @Query("""
//            SELECT o
//            FROM SalesOrder o
//            WHERE DATE(o.salesOrderDatetime) = :salesOrderDate
//""")
//    List<SalesOrder> getSalesOrderbyDate(@Param("salesOrderDate") LocalDate salesOrderDate);
//
//    @Query("""
//            SELECT o
//            FROM SalesOrder o
//            where o.store.storeNo = :storeNo
//""")
//    List<SalesOrder> getSalesOrdersByStore(@Param("storeNo") Long storeNo);
//
//
//    @Query("""
//            select o
//            from SalesOrder o
//            where o.store.storeNo = :storeNo
//              and DATE(o.salesOrderDatetime) = :salesOrderDate
//""")
//    List<SalesOrder> getSalesOrderByStoreAndDate(@Param("storeNo") Long storeNo,
//                                                 @Param("salesOrderDate") LocalDate salesOrderDate);
//
//
    @Query("""
    SELECT SUM(sod.menuCount)
    FROM StoreOrderDetail sod
    JOIN sod.salesOrder so
    WHERE so.store.storeNo = :storeNo
      AND so.salesOrderDatetime BETWEEN :start AND :end
""")
    Integer getStoreMenuCount(
            @Param("storeNo") Long storeNo,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );
}
