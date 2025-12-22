package com.erp.auth.Filter;

import com.nimbusds.jose.JWSAlgorithm;

public interface JwtProperties {
    String SECRET = "my-super-long-secret-key-at-least-32-bytes!"; // 우리 서버에서만 알고 있는 비밀 값

    int EXPIRATION_TIME = 1000 * 60 * 10; // 10분

    String TOKEN_PREFIX = "Bearer ";

    String HEADER_STRING = "Authorization";

    JWSAlgorithm ALGORITHM = JWSAlgorithm.HS256;
}
