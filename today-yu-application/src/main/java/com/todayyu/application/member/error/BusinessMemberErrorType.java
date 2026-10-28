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
    DUPLICATED_EMAIL(ErrorCategory.CONFLICT, "MEMBER-1001", "이미 가입된 이메일입니다."),
    NOT_FOUND(ErrorCategory.NOT_FOUND, "MEMBER-1002", "존재하지 않는 회원입니다."),
    NO_LOCAL_ACCOUNT(ErrorCategory.BUSINESS, "MEMBER-1003", "이메일로 가입한 계정만 비밀번호를 변경할 수 있습니다."),
    CURRENT_PASSWORD_MISMATCH(ErrorCategory.BUSINESS, "MEMBER-1004", "현재 비밀번호가 일치하지 않습니다."),
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
