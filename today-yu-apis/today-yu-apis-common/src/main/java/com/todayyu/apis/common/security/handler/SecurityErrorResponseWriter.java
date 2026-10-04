package com.todayyu.apis.common.security.handler;

import com.todayyu.apis.common.exception.ErrorHttpStatusResolver;
import com.todayyu.apis.common.response.ErrorResponse;
import com.todayyu.common.error.BaseErrorType;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class SecurityErrorResponseWriter {
    private final JsonMapper jsonMapper;
    
    public void write(HttpServletResponse response, BaseErrorType errorType) throws IOException {
        response.setStatus(ErrorHttpStatusResolver.resolve(errorType.getCategory()).value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        jsonMapper.writeValue(response.getWriter(), ErrorResponse.from(errorType));
    }
}
