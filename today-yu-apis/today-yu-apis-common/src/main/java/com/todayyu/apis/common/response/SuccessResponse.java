package com.todayyu.apis.common.response;

import lombok.Getter;

@Getter
public class SuccessResponse {
    private final String message;
    private final Object data;
    
    public SuccessResponse(String message, Object data) {
        this.message = message;
        this.data = data;
    }
    
    public SuccessResponse(String message) {
        this(message, null);
    }
    
    
    public static SuccessResponse from(String message) {
        return new SuccessResponse(message);
    }
    
    public static SuccessResponse from(String message, Object data) {
        return new SuccessResponse(message, data);
    }
}
