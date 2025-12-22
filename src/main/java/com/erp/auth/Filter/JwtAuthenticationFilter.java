package com.erp.auth.Filter;


import com.erp.auth.PrincipalDetails;
import com.erp.dto.ManagerDTO;
import com.erp.response.ApiResponse;
import com.erp.response.ErrorCode;
import com.erp.response.ErrorResponse;
import com.erp.response.ResponseUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.util.Date;
import java.util.Map;

@Log4j2
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {
    private final AuthenticationManager authenticationManager;

    // 인증 시도 메서드 재정의
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        log.info("로그인 시도 - 사용자 정보 인증");
        try {
            // 로그인 요청 시 body에 담겨온 사용자 정보를 ManagerDTO 객체에 매핑
            ManagerDTO input = new ObjectMapper().readValue(request.getInputStream(), ManagerDTO.class);

            // UsernamePasswordAuth의 id와 pw를 사용한 인증 token 획득
            UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(input.getManagerId(), input.getPw());

            // authManager가 token을 인증 후 Authentication 인증 객체를 반환
            Authentication auth = authenticationManager.authenticate(token);

            return auth;
        }
        catch(IOException e){
            log.error("사용자 인증 실패: {}", e.getMessage());
        }

        return null;
    }

    // 인증 성공 메서드 재정의
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {
        log.info("로그인 성공 - JWT 생성");
        // 인증 결과로부터 사용자 정보 획득
        PrincipalDetails result = (PrincipalDetails) authResult.getPrincipal();

        // JWT 빌더 객체 생성과 동시에 초기화
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .subject(result.getUsername())
                .claim("role", result.getManager().getRole())
                .issuer("my-server")
                .expirationTime(
                        new Date(System.currentTimeMillis() + JwtProperties.EXPIRATION_TIME)
                )
                .build();
        // JWT 서명 알고리즘 선택 + JWT 데이터 등록
        SignedJWT signedJWT = new SignedJWT(
                new JWSHeader(JwtProperties.ALGORITHM),
                claims
        );


        try {
            // 암호 키로 JWT 서명
            signedJWT.sign(new MACSigner(JwtProperties.SECRET));
        } catch (JOSEException e) {
            throw new RuntimeException(e);
        }
        // JWT 값 생성
        String token = signedJWT.serialize();

        log.info("로그인 성공 - JWT: {}", token);
        // 응답 헤더에 JWT 값을 추가(Authorization : ....)
        response.addHeader(JwtProperties.HEADER_STRING, JwtProperties.TOKEN_PREFIX + token);
        // 응답으로 전달할 데이터
        ResponseUtil.writeJson(
                response,
                HttpServletResponse.SC_OK,
                ApiResponse.success(Map.of("message", "loginOK"))
        );
    }

    // 인증 실패 메서드 재정의
    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException, ServletException {
        log.error("인증 실패: {}", failed.getMessage());

        ResponseUtil.writeJson(
                response,
                HttpServletResponse.SC_UNAUTHORIZED,
                ApiResponse.error(ErrorResponse.of(ErrorCode.AUTH_FAILED))
        );
    }
}
