package com.todayyu.domain.inquiry.error;

import com.todayyu.common.error.BaseErrorType;
import com.todayyu.common.error.ErrorCategory;
import lombok.RequiredArgsConstructor;

/**
 * Inquiry 도메인 불변식 위반 등으로 인해 발생하는 에러 타입
 *
 * <p> 에러 코드: {@code INQUIRY-0xxx} </p>
 */
@RequiredArgsConstructor
public enum InquiryErrorType implements BaseErrorType {
    FAIL_UPDATE(ErrorCategory.CONFLICT, "INQUIRY-0001", "답변 대기 중 상태의 문의만 수정할 수 있습니다."),
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
