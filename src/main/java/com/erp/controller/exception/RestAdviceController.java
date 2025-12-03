package com.erp.controller.exception;

import com.erp.controller.StoreItemRestController;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice(assignableTypes = StoreItemRestController.class)
public class RestAdviceController {
    @ExceptionHandler(StoreItemNotFoundException.class)
    public ResponseEntity<Map<String, String>> StoreItemNotFoundException(StoreItemNotFoundException e) {
        return ResponseEntity.status(404)
                .body(Map.of("message", e.getMessage()));
    }

    @ExceptionHandler(StoreItemLimitConflictException.class)
    public ResponseEntity<Map<String, String>> StoreItemLimitConflictException(StoreItemLimitConflictException e) {
        return ResponseEntity.status(409)
                .body(Map.of("message", e.getMessage()));
    }

    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<Map<String, String>> NullPointerException(Exception e){
        return ResponseEntity.status(400).body(Map.of("message", e.getMessage()));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> RuntimeException(RuntimeException e) {
        return ResponseEntity.status(500).body(Map.of("message", e.getMessage()));
    }
}
