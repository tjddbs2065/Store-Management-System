package com.erp.exception;

import lombok.Getter;

@Getter
public class ItemOrderException extends RuntimeException { // 발주 정보에 대한 Exception
    private final ItemOrderErrorCode errorCode;

    public ItemOrderException(ItemOrderErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
