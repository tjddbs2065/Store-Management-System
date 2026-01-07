package com.erp.dto;

import com.erp.repository.entity.ItemProposal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ItemProposalDTO {
    private Long proposalNo;
    private String managerNo;
    private String managerName;

    private Long storeNo;
    private String storeName;

    private Long itemNo;
    private String itemName;
    private Integer itemPrice;

    private Integer quantity;
    private String supplyUnit;
    private Integer convertStock;
    private String stockUnit ;
    private String reason;
    private Timestamp proposalDate;
    private Timestamp responseDate;
    public static ItemProposalDTO from(ItemProposal prop) {
        return ItemProposalDTO.builder()
                .proposalNo(prop.getItemProposalNo())
                .managerNo(prop.getManagerId().getManagerId())
                .managerName(prop.getManagerId().getManagerName())
                .storeNo(prop.getStoreNo().getStoreNo())
                .storeName(prop.getStoreNo().getStoreName())
                .itemNo(prop.getItemNo().getItemNo())
                .itemName(prop.getItemNo().getItemName())
                .quantity(prop.getProposalQuantity())
                .supplyUnit(prop.getItemNo().getSupplyUnit())
                .stockUnit(prop.getItemNo().getStockUnit())
                .convertStock(prop.getItemNo().getConvertStock())
                .reason(prop.getProposalReason())
                .proposalDate(prop.getProposalDate())
                .responseDate(prop.getResponseDate())
                .itemPrice(prop.getItemNo().getItemPrice())
                .build();
    }
}
