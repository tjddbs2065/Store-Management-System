package com.erp.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ItemOrderErrorCode { // 발주 정보에 대한 에러 코드
    ITEM_ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "발주 정보를 찾을 수 없습니다."),
    ITEM_ORDER_INVALID_VALUE(HttpStatus.BAD_REQUEST , "잘못된 발주 정보입니다.");

    private final HttpStatus status;
    private final String message;

    ItemOrderErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }
}
