package com.erp.exception;

public class ItemOrderNotFoundException extends RuntimeException {
    public ItemOrderNotFoundException(String message) {
        super(message);
    }
}
