package com.todayyu.application.member.error;

import com.todayyu.common.error.BaseErrorType;
import com.todayyu.common.error.ErrorCategory;
import lombok.RequiredArgsConstructor;

/**
 * Member 도메인에서 비즈니스 로직 처리 중 발생하는 에러 타입
 *
 * <p> 에러 코드: {@code MEMBER-1xxx}</p>
 */
@RequiredArgsConstructor
public enum BusinessMemberErrorType implements BaseErrorType {
    DUPLICATED_EMAIL(ErrorCategory.CONFLICT, "MEMBER-1000", "이미 가입된 이메일입니다."),
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
