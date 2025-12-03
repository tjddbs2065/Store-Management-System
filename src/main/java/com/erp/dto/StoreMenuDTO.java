package com.erp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StoreMenuDTO {
    private Long storeMenuNo;
    private String storeName;
    private String menuCode;
    private String menuName;
    private String size;
    private Integer menuPrice;
    private String salesStatus;
    private String menuCategory;
}