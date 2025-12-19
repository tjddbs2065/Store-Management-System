package com.erp.auth.Filter;

import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;

@Configuration
@Log4j2
public class CorsConfig {
    @Bean
    public CorsFilter corsFilter() {
        log.info("CorsFilter");

        // Cors 설정 객체 생성
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true); // 브라우저가 인증 정보가 포함된 요청(쿠키, Authorization 헤더 등)을 허용하도록 설정
        config.addAllowedOriginPattern("*"); // 모든 Origin을 허용하되, 패턴 기반으로 허용
        config.addAllowedHeader("*"); // 클라이언트가 어떤 요청 헤더(Authorization 같은 커스텀 헤더)든 보낼 수 있도록 허용
        config.addAllowedMethod("*"); // 모든 HTTP 메서드(GET, POST, PUT, DELETE 등) 허용
        config.setExposedHeaders(Arrays.asList(
                "Access-Control-Allow-Headers",
                "Authorization"
        )); // 브라우저의 js 코드가 응답 헤더를 읽을 수 있도록 허용(여기서는 Authorization 헤더)

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config); // 모든 경로에 대해 이 cors 정책을 적용(= 모든 경로에서 이 cors 정책을 적용)

        return new CorsFilter(source);
    }
}
