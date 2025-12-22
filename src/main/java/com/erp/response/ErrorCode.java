package com.erp.response;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    // 인증 관련 오류
    AUTH_FAILED(HttpStatus.UNAUTHORIZED, "AUTH_000", "인증에 실패했습니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "AUTH_001", "인증이 필요합니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "AUTH_002", "접근 권한이 없습니다."),
    AUTH_EXPIRED(HttpStatus.REQUEST_TIMEOUT, "AUTH_003", "인증이 만료되었습니다."),
    AUTH_NOT_VERIFY(HttpStatus.NOT_FOUND, "AUTH_004", "알 수 없는 인증입니다."),
    AUTH_NOT_CONVERTABLE(HttpStatus.NOT_ACCEPTABLE, "AUTH_005", "사용할 수 없는 인증입니다."),

    // 요청 관련 오류
    REQ_NOT_FOUND(HttpStatus.NOT_FOUND, "REQ_001", "찾을 수 없는 페이지 입니다."),

    // 서버 관련 오류
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "RES_001", "서버 에러입니다.");

    
    private final HttpStatus status;
    private final String errorCode;
    private final String errorMessage;
    ErrorCode(HttpStatus status, String errorCode, String errorMessage){
        this.status = status;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }
}
