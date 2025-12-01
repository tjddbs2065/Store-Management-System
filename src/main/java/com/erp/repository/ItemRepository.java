package com.erp.repository;

import com.erp.dto.ItemStoreQuantityDTO;
import com.erp.repository.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {

    Item getItemByIngredientName(String ingredientName);

    @Query("""
    select new com.erp.dto.ItemStoreQuantityDTO(
        i.itemNo,
        i.itemCode,
        i.itemCategory,
        i.itemName,
        i.stockUnit,
        i.supplyUnit,
        i.supplier,
        i.itemPrice,
        si.storeLimit,
        ss.currentQuantity
    )
    from Item i
    left join StoreItem si on i.itemNo = si.itemNo and si.storeNo = :storeNo
    left join StoreStock ss on ss.storeStockNo = (
        select max(ss2.storeStockNo)
        from StoreStock ss2
        where ss2.storeItemNo = si.storeItemNo
        )
    """)
    List<ItemStoreQuantityDTO> findAllWithQuantity(Long storeNo);
}
