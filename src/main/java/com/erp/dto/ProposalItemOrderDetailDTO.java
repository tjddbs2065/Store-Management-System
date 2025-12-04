package com.erp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ProposalItemOrderDetailDTO {
    private Long itemNo;
    private Integer itemOrderPrice;
    private Integer itemQuantity;
    private String proposalReason;
}
