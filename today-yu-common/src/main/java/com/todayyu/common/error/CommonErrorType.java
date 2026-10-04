package com.todayyu.common.error;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum CommonErrorType implements BaseErrorType {
    SERVER_ERROR(ErrorCategory.SERVER, "COMMON-0001", "서버 내부에서 에러가 발생하였습니다."),
    VALIDATION_ERROR(ErrorCategory.VALIDATION, "COMMON-0002", "요청값이 유효하지 않습니다."),
    BAD_REQUEST(ErrorCategory.BUSINESS, "COMMON-0003", "유효하지 않은 요청입니다."),
    NOT_FOUND(ErrorCategory.NOT_FOUND, "COMMON-0004", "존재하지 않는 리소스입니다."),
    UNAUTHORIZED(ErrorCategory.UNAUTHORIZED, "COMMON-0005", "인증이 필요합니다."),
    FORBIDDEN(ErrorCategory.FORBIDDEN, "COMMON-0006", "접근 권한이 없습니다."),
    ;
    
    private final ErrorCategory category;
    private final String errorCode;
    private final String message;
    
    @Override
    public ErrorCategory getCategory() {
        return category;
    }
    
    @Override
    public String getErrorCode() {
        return errorCode;
    }
    
    @Override
    public String getMessage() {
        return message;
    }
}
