package com.todayyu.apis.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.todayyu.common.error.BaseErrorType;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {
    private final String errorCode;
    private final String message;
    private final Map<String, String> errors;
    private final Object data;
    
    @Builder
    public ErrorResponse(String errorCode, String message, Map<String, String> errors, Object data) {
        this.errorCode = errorCode;
        this.message = message;
        this.errors = errors;
        this.data = data;
    }
    
    public static ErrorResponse from(BaseErrorType errorType) {
        return ErrorResponse.builder()
                .errorCode(errorType.getErrorCode())
                .message(errorType.getMessage())
                .build();
    }
    
    public static ErrorResponse from(BaseErrorType errorType, Object data) {
        return ErrorResponse.builder()
                .errorCode(errorType.getErrorCode())
                .message(errorType.getMessage())
                .data(data)
                .build();
    }
    
    public static ErrorResponse withErrors(BaseErrorType errorType, Map<String, String> errors) {
        return ErrorResponse.builder()
                .errorCode(errorType.getErrorCode())
                .message(errorType.getMessage())
                .errors(errors)
                .build();
    }
}
