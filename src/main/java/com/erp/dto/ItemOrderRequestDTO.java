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
public class ItemOrderRequestDTO {

    private Long storeNo;
    private int totalItem;
    private int totalAmount;

    private List<OrderItemDTO> orderList;
}