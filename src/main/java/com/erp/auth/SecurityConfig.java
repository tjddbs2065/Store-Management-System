package com.erp.auth;

import com.erp.auth.Filter.JwtAuthenticationFilter;
import com.erp.auth.Filter.JwtAuthorizationFilter;
import com.erp.dao.ManagerDAO;
import com.erp.response.ApiResponse;
import com.erp.response.ErrorCode;
import com.erp.response.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.web.filter.CorsFilter;

import java.io.IOException;


@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true)
@RequiredArgsConstructor
@Log4j2
public class SecurityConfig {

    private final LoginSuccessHandler loginSuccessHandler;

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration ac) throws Exception {
        return ac.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, AuthenticationManager am, CorsFilter corsFilter, ManagerDAO managerDAO, ObjectMapper objectMapper) throws Exception {

        // csrf 보안 비활성화
        http.csrf(AbstractHttpConfigurer::disable);

        http.sessionManagement(session->{
            session.sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        }); // 기본 Stateful인 세션 설정을 Stateless로 세션 정책 변경(= 세션 정보 관리 비활성화)

        // 인증 방식이 혼용될 수 있기에 하나만 유지하기 위해 비활성화(+ 불필요 처리)
        http.formLogin(AbstractHttpConfigurer::disable); // UsernamePasswordAuthorizationFilter 비활성화
        http.logout(AbstractHttpConfigurer::disable); // LogoutFilter 비활성화(session 정보 invalidate)
        http.httpBasic(AbstractHttpConfigurer::disable); // HttpBasicAuthorizationFilter 비활성화
        // basic authentication:  username과 password 정보를 base64로 인코딩하여 http 헤더의 Authorization에 basic 키워드와 함께 전달해 인증하는 방식


        http.addFilter(corsFilter); // CorsConfig에서 Bean으로 등록한 corsFilter를 등록
        http.addFilter(new JwtAuthenticationFilter(am)); // 인증 전 jwt 토큰 발급 처리 필터 등록
        http.addFilter(new JwtAuthorizationFilter(am, managerDAO, objectMapper)); // 인증 후 jwt 토큰 검증 후 데이터 추출

//        http.authorizeHttpRequests(auth -> {
//            auth.anyRequest().permitAll();
//        });

        http.authorizeHttpRequests(
                auth -> auth
                .requestMatchers("/error").permitAll()
                .requestMatchers("/image/**", "/css/**", "/js/**").permitAll()
                .requestMatchers("/loginView").permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/manager/**").hasAnyRole("ADMIN", "MANAGER")
                .requestMatchers("/store/**").hasRole("STORE")
                .anyRequest().authenticated()
        );


        // UsernamePasswordAuthorizationFilter를 사용하지 않기에 불필요
//        http.formLogin(form -> form
//            .loginPage("/loginView")
//            .loginProcessingUrl("/login")
//            .usernameParameter("managerId")
//            .passwordParameter("pw")
//            .successHandler(loginSuccessHandler)
//            .failureUrl("/loginView")
//        );

        http.exceptionHandling(ex -> {
           ex.accessDeniedHandler(new AccessDeniedHandler() {
               @Override
               public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
                   log.info("요청 상태 - 권한 부족: {}", accessDeniedException.getMessage());

                   response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                   response.setContentType("application/json;charset=UTF-8");
                   ErrorResponse errorResponse = ErrorResponse.of(ErrorCode.FORBIDDEN);

                   new ObjectMapper().writeValue(response.getOutputStream(), ApiResponse.error(ErrorResponse.of(ErrorCode.FORBIDDEN)));

//                   request.getRequestDispatcher("/noPermission").forward(request, response);
               }
           });
        });
//        http.logout(logout -> logout
//                .logoutUrl("/logout")
//                .logoutSuccessUrl("/loginView")
//                .invalidateHttpSession(true)
//        );

        return http.build();
    }
}
