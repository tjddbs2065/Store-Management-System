package com.erp.repository;

import com.erp.dto.ItemOrderDTO;
import com.erp.repository.entity.ItemOrder;
import com.erp.repository.entity.Store;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface ItemOrderRepository extends JpaRepository<ItemOrder, Long> {

    @Query("""
    select new com.erp.dto.ItemOrderDTO(
        io.itemOrderNo,
        st.storeNo,
        st.storeName,
        io.requestDatetime,
        io.totalItem,
        io.totalAmount,
        io.itemOrderStatus,
        case 
            when count(od) = sum(case when od.receiveDatetime is not null then 1 else 0 end)
            then '입고완료'
            else '입고대기'
        end
    )
    from ItemOrder io
    left join io.storeNo st
    left join ItemOrderDetail od on od.itemOrderNo.itemOrderNo = io.itemOrderNo
    where (
        ((:start is null or io.requestDatetime >= :start) and (:end is null or io.requestDatetime <= :end))
        or io.itemOrderStatus = :status)
    group by io.itemOrderNo, st.storeNo, st.storeName,
                io.requestDatetime, io.totalItem,
                io.totalAmount, io.itemOrderStatus
""")
    Page<ItemOrderDTO> findAllItemOrderList(Pageable pageable, String status, LocalDateTime start, LocalDateTime end);

    @Query("""
    select new com.erp.dto.ItemOrderDTO(
        io.itemOrderNo,
        st.storeNo,
        st.storeName,
        io.requestDatetime,
        io.totalItem,
        io.totalAmount,
        io.itemOrderStatus,
        case 
            when count(od) = sum(case when od.receiveDatetime is not null then 1 else 0 end)
            then '입고완료'
            else '입고대기'
        end
    )
    from ItemOrder io
    left join io.storeNo st
    left join ItemOrderDetail od on od.itemOrderNo.itemOrderNo = io.itemOrderNo
    where io.requestDatetime between :start and :end
    group by io.itemOrderNo, st.storeNo, st.storeName,
                io.requestDatetime, io.totalItem,
                io.totalAmount, io.itemOrderStatus
""")
    Page<ItemOrderDTO> findByRequestDatetimeBetween(LocalDateTime start, LocalDateTime end, Pageable pageable);

    @Query("""
    select new com.erp.dto.ItemOrderDTO(
        io.itemOrderNo,
        st.storeNo,
        st.storeName,
        io.requestDatetime,
        io.totalItem,
        io.totalAmount,
        io.itemOrderStatus,
        case 
            when count(od) = sum(case when od.receiveDatetime is not null then 1 else 0 end)
            then '입고완료'
            else '입고대기'
        end
    )
    from ItemOrder io
    left join io.storeNo st
    left join ItemOrderDetail od on od.itemOrderNo.itemOrderNo = io.itemOrderNo
    where function('DAYOFWEEK', io.requestDatetime) = :day
    group by io.itemOrderNo, st.storeNo, st.storeName,
                io.requestDatetime, io.totalItem,
                io.totalAmount, io.itemOrderStatus
""")
    Page<ItemOrderDTO> findByRequestDatetimeDay(@Param("day") int day, Pageable pageable);

    @Query("""
    select new com.erp.dto.ItemOrderDTO(
        io.itemOrderNo,
        st.storeNo,
        st.storeName,
        io.requestDatetime,
        io.totalItem,
        io.totalAmount,
        io.itemOrderStatus,
        case 
            when count(od) = sum(case when od.receiveDatetime is not null then 1 else 0 end)
            then '입고완료'
            else '입고대기'
        end
    )
    from ItemOrder io
    left join io.storeNo st
    left join ItemOrderDetail od on od.itemOrderNo.itemOrderNo = io.itemOrderNo
    where io.storeNo = :storeNo
    group by io.itemOrderNo, st.storeNo, st.storeName,
                io.requestDatetime, io.totalItem,
                io.totalAmount, io.itemOrderStatus
""")
    Page<ItemOrderDTO> findByStoreNo(Store storeNo, Pageable pageable);

    @Query("""
    select new com.erp.dto.ItemOrderDTO(
        io.itemOrderNo,
        st.storeNo,
        st.storeName,
        io.requestDatetime,
        io.totalItem,
        io.totalAmount,
        io.itemOrderStatus,
        case 
            when count(od) = sum(case when od.receiveDatetime is not null then 1 else 0 end)
            then '입고완료'
            else '입고대기'
        end
    )
    from ItemOrder io
    left join io.storeNo st
    left join ItemOrderDetail od on od.itemOrderNo.itemOrderNo = io.itemOrderNo
    where io.itemOrderStatus = :status
    group by io.itemOrderNo, st.storeNo, st.storeName,
                io.requestDatetime, io.totalItem,
                io.totalAmount, io.itemOrderStatus
""")
    Page<ItemOrderDTO> findByItemOrderStatus(String status, Pageable pageable);

    List<ItemOrder> findByItemOrderStatusAndStoreNo(String status, Store storeNo);

    ItemOrder findByItemOrderNo(long l);

}
