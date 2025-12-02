package com.erp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class SalesOrderRequestDTO {
    private Long storeNo;
    private List<StoreMenuDTO> menuList;
    private List<SalesOrderDetailDTO> detailList;
}
