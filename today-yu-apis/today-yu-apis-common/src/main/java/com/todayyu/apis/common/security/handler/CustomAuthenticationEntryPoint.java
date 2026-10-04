package com.todayyu.apis.common.security.handler;

import com.todayyu.common.error.BaseErrorType;
import com.todayyu.common.error.CommonErrorType;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {
    private final SecurityErrorResponseWriter responseWriter;
    
    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException, ServletException {
        BaseErrorType errorType = CommonErrorType.UNAUTHORIZED;
        log.warn("[Error/Security] 인증 실패 - {}, {}", request.getMethod(), request.getRequestURI());
        
        responseWriter.write(response, errorType);
    }
}
