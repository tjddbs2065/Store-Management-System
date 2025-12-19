package com.erp.controller;

import com.erp.response.ApiResponse;
import com.erp.response.ErrorCode;
import com.erp.response.ErrorResponse;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CommonController implements ErrorController {
    @RequestMapping("/error")
    public ResponseEntity<ApiResponse<Object>> handleError(HttpServletRequest request) {
        Object statusCode = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        int status = statusCode != null ? Integer.parseInt(statusCode.toString()) : HttpStatus.INTERNAL_SERVER_ERROR.value();

        if(status == HttpStatus.NOT_FOUND.value()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(ErrorResponse.of(ErrorCode.REQ_NOT_FOUND)));
        }
        if(status == HttpStatus.FORBIDDEN.value()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error(ErrorResponse.of(ErrorCode.FORBIDDEN)));
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(ErrorResponse.of(ErrorCode.INTERNAL_SERVER_ERROR)));
    }
}
