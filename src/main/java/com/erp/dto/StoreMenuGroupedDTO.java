package com.erp.dto;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class StoreMenuGroupedDTO {
    private String menuCode;
    private String menuName;

    private List<StoreMenuDTO> items = new ArrayList<>();
}