package com.erp.response;

import lombok.Getter;

@Getter
public class ApiResponse<T> {
    private final boolean success;
    private T data;
    private final ErrorResponse error;

    private ApiResponse(boolean success, T data, ErrorResponse error) {
        this.success = success;
        this.data = data;
        this.error = error;
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<T>(true, data, null);
    }

    public static <T> ApiResponse<T> error(ErrorResponse error) {
        return new ApiResponse<T>(false, null, error);
    }
}
