package com.todayyu.domain.member.error;

import com.todayyu.common.error.BaseErrorType;
import com.todayyu.common.error.ErrorCategory;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum MemberErrorType implements BaseErrorType {
    VERIFICATION_CODE_REISSUE_TOO_EARLY(ErrorCategory.TOO_MANY_REQUESTS, "MEMBER-0001", "이메일 인증 코드는 발급 후 1분이 지나야 재발급할 수 있습니다."),
    EMAIL_VERIFICATION_FAIL(ErrorCategory.BUSINESS, "MEMBER-0002", "이메일 인증 코드 검증에 실패하였습니다."),
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
