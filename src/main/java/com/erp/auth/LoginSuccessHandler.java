package com.erp.auth;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class    LoginSuccessHandler implements AuthenticationSuccessHandler {
    //로그인 했을 때 추가 작업들
    //지금은 로그인후 첫 페이지 url 처리하는거만 있어요

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {

        String role = authentication.getAuthorities().iterator().next().getAuthority();

        if ("ROLE_ADMIN".equals(role) || "ROLE_MANAGER".equals(role)) {
            response.sendRedirect("/manager/salesMain");

        } else if ("ROLE_STORE".equals(role)) {
            response.sendRedirect("/store/storeSalesMain");
        }

    }
}
