package com.erp.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class KPIDTO {
    private Integer totalSales;
    private Integer totalMenuCount;
    private Integer avgStoreSales;
    private Integer avgOrderAmount;
    private Double growthRate;
}