package com.erp.controller.exception;

public class InvalidDateRangeException extends RuntimeException {
    public InvalidDateRangeException() {
        super("시작날짜가 완료날짜보다 늦을수없다.");
    }
}
