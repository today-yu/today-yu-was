package com.todayyu.apis.common.security.config;

import com.todayyu.apis.common.security.handler.CustomAccessDeniedHandler;
import com.todayyu.apis.common.security.handler.CustomAuthenticationEntryPoint;
import com.todayyu.apis.common.security.handler.SecurityErrorResponseWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;

@Configuration
public class SecurityHandlerConfig {
    @Bean
    public AccessDeniedHandler accessDeniedHandler(SecurityErrorResponseWriter writer) {
        return new CustomAccessDeniedHandler(writer);
    }
    
    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint(SecurityErrorResponseWriter writer) {
        return new CustomAuthenticationEntryPoint(writer);
    }
}
