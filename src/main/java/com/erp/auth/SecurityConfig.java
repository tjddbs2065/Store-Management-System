package com.erp.auth;

import com.erp.auth.LoginSuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;


@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final LoginSuccessHandler loginSuccessHandler;

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http.csrf(csrf -> csrf.disable());

        http.authorizeHttpRequests(auth ->
                auth.requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/manager/**").hasAnyRole("ADMIN", "MANAGER")
                .requestMatchers("/store/**").hasRole("STORE").
                anyRequest().permitAll());


        http.formLogin(form -> form
                .loginPage("/loginView")
                .loginProcessingUrl("/login")
                .usernameParameter("managerId")
                .passwordParameter("pw")
                .successHandler(loginSuccessHandler)
                .failureUrl("/loginView")
        );

//        http.logout(logout -> logout
//                .logoutUrl("/logout")
//                .logoutSuccessUrl("/loginView")
//                .invalidateHttpSession(true)
//        );

        return http.build();
    }
}
