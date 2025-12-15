package com.erp.dto;

import lombok.*;

import java.sql.Timestamp;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
@ToString
@Getter
@Setter
@Builder
public class MenuDTO {
    private Long menuNo;
    private String menuName;
    private String menuCode;
    private String menuCategory;
    private String menuExplain;
    private String size;
    private String menuImage;
    private int menuPrice;
    private String releaseStatus;
    private Timestamp inDate;
    private Timestamp editDate;
    private Timestamp delDate;
    private Integer menuPriceLarge;
    private Integer menuPriceMedium;

    private String originImage;
    private boolean removeImage;
    private List<MenuIngredientDTO> ingredients;
    private List<MenuDTO> sizeList;   // 같은 코드의 메뉴(L/M/단일)를 모두 포함
    private boolean hasSize;
}
