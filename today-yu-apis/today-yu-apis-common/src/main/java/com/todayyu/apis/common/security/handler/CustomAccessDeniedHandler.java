package com.todayyu.apis.common.security.handler;

import com.todayyu.common.error.BaseErrorType;
import com.todayyu.common.error.CommonErrorType;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class CustomAccessDeniedHandler implements AccessDeniedHandler {
    private final SecurityErrorResponseWriter responseWriter;
    
    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException
    ) throws IOException, ServletException {
        BaseErrorType errorType = CommonErrorType.FORBIDDEN;
        log.warn("[Error/Security] 인가 실패 - {} / {}", request.getMethod(), request.getRequestURI());
        
        responseWriter.write(response, errorType);
    }
}
