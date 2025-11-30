package com.erp.controller.exception;

public class ItemOrderNotFoundException extends RuntimeException {
    public ItemOrderNotFoundException(Long itemOrderNo) {
        super("존재하지 않는 발주입니다. ItemOrder = " + itemOrderNo);
    }
}
