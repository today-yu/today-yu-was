package com.todayyu.application.inquiry.error;

import com.todayyu.common.error.BaseErrorType;
import com.todayyu.common.error.ErrorCategory;
import lombok.RequiredArgsConstructor;

/**
 * Inquiry 도메인에서 비즈니스 로직 처리 중 발생하는 에러 타입
 *
 * <p> 에러 코드: {@code INQUIRY-1xxx} </p>
 */
@RequiredArgsConstructor
public enum BusinessInquiryErrorType implements BaseErrorType {
    NOT_FOUND(ErrorCategory.NOT_FOUND, "INQUIRY-1001","문의가 존재하지 않습니다."),
    FORBIDDEN(ErrorCategory.FORBIDDEN, "INQUIRY-1002","문의에 접근 권한이 없습니다."),
    ;
    
    private final ErrorCategory category;
    private final String code;
    private final String message;
    
    @Override
    public ErrorCategory getCategory() {
        return category;
    }
    
    @Override
    public String getErrorCode() {
        return code;
    }
    
    @Override
    public String getMessage() {
        return message;
    }
}
