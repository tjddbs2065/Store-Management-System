package com.erp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ItemStoreQuantityDTO {
    private Long itemNo;
    private String itemCode;
    private String itemCategory;
    private String itemName;

    private String stockUnit;
    private String supplyUnit;
    private String supplier;
    private Integer itemPrice;

    private Integer itemQuantity;
    private Integer limit;
}
