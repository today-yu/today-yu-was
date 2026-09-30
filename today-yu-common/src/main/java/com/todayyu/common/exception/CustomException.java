package com.todayyu.common.exception;

import com.todayyu.common.error.BaseErrorType;
import lombok.Getter;

@Getter
public class CustomException extends RuntimeException {
    private final BaseErrorType errorType;
    private final Object data;
    
    public CustomException(BaseErrorType errorType, Throwable cause, Object data) {
        super(errorType.getMessage(), cause);
        this.errorType = errorType;
        this.data = data;
    }
    
    public CustomException(BaseErrorType errorType, Throwable cause) {
        super(errorType.getMessage(), cause);
        this.errorType = errorType;
        this.data = null;
    }
    
    public CustomException(BaseErrorType errorType, String detailMessage) {
        super(detailMessage);
        this.errorType = errorType;
        this.data = null;
    }
    
    public CustomException(BaseErrorType errorType) {
        super(errorType.getMessage(), null);
        this.errorType = errorType;
        this.data = null;
    }
}
