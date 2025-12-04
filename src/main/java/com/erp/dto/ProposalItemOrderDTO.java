package com.erp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class ProposalItemOrderDTO {
    private String managerId;
    private Long storeNo;
    private Integer totalItem;
    private Integer totalPrice;

    private List<ProposalItemOrderDetailDTO> proposalList;
}
