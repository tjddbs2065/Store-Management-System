package com.erp.exception;

public class StoreItemNotFoundException extends RuntimeException {

    public StoreItemNotFoundException(String message) {
        super(message);
    }
}
