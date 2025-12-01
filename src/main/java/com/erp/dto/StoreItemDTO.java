package com.erp.dto;

import lombok.*;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class StoreItemDTO {
    private Long   storeItemNo;
    private Long   storeNo;
    private String storeName;
    private Long   itemNo;
    private String itemCode;
    private String itemName;
    private String itemCategory;
    private Integer finalLimit;
    private String  limitOwner;      // "MANAGER" / "STORE" / "NONE"
    private Integer currentQuantity;      // COALESCE(...,0)
    private String  stockUnit;
    private Integer convertStock;
    private Integer managerLimit;
    private Integer storeLimit;

}