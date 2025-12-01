package com.erp.repository;

import com.erp.dto.ItemOrderDetailDTO;
import com.erp.repository.entity.ItemOrder;
import com.erp.repository.entity.ItemOrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ItemOrderDetailRepository extends JpaRepository<ItemOrderDetail, Long> {

    List<ItemOrderDetail> findByItemOrderNo(ItemOrder itemOrderNo);

    @Query("""
        select new com.erp.dto.ItemOrderDetailDTO(
                id.itemOrderDetailNo,
                i.itemCode,
                i.itemName,
                i.itemCategory,
                id.orderDetailQuantity,
                i.supplyUnit,
                i.itemPrice,
                id.orderDetailPrice,
                id.receiveDatetime
            )
            from ItemOrderDetail id
            left join id.itemNo i
            where id.itemOrderNo=:itemOrder
    """)
    List<ItemOrderDetailDTO> findAllItemOrderDetail(ItemOrder itemOrder);

    ItemOrderDetail findByItemOrderDetailNo(Long itemOrderDetailNo);
}