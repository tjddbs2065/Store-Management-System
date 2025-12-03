package com.erp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StoreMenuSalesSummaryDTO {

    private String storeName;
    private String menuCategory;
    private String menuName;
    private String size;
    private Long menuCount;
    private Long totalPrice;


}