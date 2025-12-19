package com.erp.auth.Filter;

import com.erp.auth.PrincipalDetails;
import com.erp.dao.ManagerDAO;
import com.erp.response.ApiResponse;
import com.erp.response.ErrorCode;
import com.erp.response.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.jwt.proc.BadJWTException;
import com.nimbusds.jwt.proc.DefaultJWTClaimsVerifier;
import com.nimbusds.jwt.proc.JWTClaimsSetVerifier;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import java.io.IOException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.HashSet;

@Log4j2
public class JwtAuthorizationFilter extends BasicAuthenticationFilter {
    private ManagerDAO managerDAO;
    private ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        log.info("검증 시도 - URI: {}", request.getRequestURI());

        String jwtToken = request.getHeader(JwtProperties.HEADER_STRING);

        if(jwtToken == null || jwtToken.isEmpty()){
            chain.doFilter(request, response);
            return;
        }

        jwtToken = jwtToken.replace(JwtProperties.TOKEN_PREFIX, "");
        log.info("검증 시도 - JWT Token: {}", jwtToken);

        try {
            // 토큰 Parsing(base64 디코딩, header/payload/signature 분리)
            SignedJWT jwt = SignedJWT.parse(jwtToken);

            // 토큰 위변조 점검
            boolean verified = jwt.verify(
                    new MACVerifier(JwtProperties.SECRET)
            );
            if (!verified) {
                throw new RuntimeException("Invalid JWT Token");
            }

            // Claims 획득(payload 접근 가능 - exp, sub, iss 등 확인 가능)
            JWTClaimsSet claims = jwt.getJWTClaimsSet();

            //
            JWTClaimsSetVerifier<SecurityContext> verifier = new DefaultJWTClaimsVerifier<>(
                    new JWTClaimsSet.Builder().issuer("my-server") .build(),
                    new HashSet<>(Arrays.asList("exp", "sub")) // jwt에 반드시 존재해야 하는 claim 이름 목록
            );
            verifier.verify(claims, null);

            String subject = claims.getSubject();
            String role = claims.getStringClaim("role");

            log.info("검증 결과 - subject: {}, role: {}", subject, role);

            if(subject != null){
                // 인증 정보 객체 생성
                PrincipalDetails details
                        = new PrincipalDetails(managerDAO.getManagerForLogin(subject));

                // 인증 상태 객체 생성
                Authentication auth
                        = new UsernamePasswordAuthenticationToken(details, null, details.getAuthorities());

                // 한 요청에 대한 Security 임시 저장소에 인증상태 등록
                SecurityContextHolder.getContext().setAuthentication(auth);
                // 나머지 필터 이어서 처리
                chain.doFilter(request, response);
            }
        }
        catch (JOSEException e){
            log.error("JWT 토큰 검증 중 문제 발생: {}", e.getMessage());
            new ObjectMapper().writeValue(response.getOutputStream(), ApiResponse.error(ErrorResponse.of(ErrorCode.AUTH_NOT_VERIFY)));
        }
        catch (ParseException e){
            log.error("JWT 토큰 변환 중 문제 발생: {}", e.getMessage());
            new ObjectMapper().writeValue(response.getOutputStream(), ApiResponse.error(ErrorResponse.of(ErrorCode.AUTH_NOT_CONVERTABLE)));
        }
        catch (BadJWTException e){
            log.error("JWT 정보 검증 중 문제 발생: {}", e.getMessage());
            new ObjectMapper().writeValue(response.getOutputStream(), ApiResponse.error(ErrorResponse.of(ErrorCode.AUTH_EXPIRED)));
        }
    }

    public JwtAuthorizationFilter(AuthenticationManager authenticationManager, ManagerDAO managerDAO, ObjectMapper objectMapper) {
        super(authenticationManager);
        this.managerDAO = managerDAO;
        this.objectMapper = objectMapper;
    }
}
