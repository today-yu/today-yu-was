package com.todayyu.application.auth.error;

import com.todayyu.common.error.BaseErrorType;
import com.todayyu.common.error.ErrorCategory;
import lombok.RequiredArgsConstructor;

/**
 * Auth 도메인에서 비즈니스 로직 처리 중 발생하는 에러 타입
 *
 * <p> 에러 코드: {@code AUTH-1xxx}</p>
 */
@RequiredArgsConstructor
public enum BusinessAuthErrorType implements BaseErrorType {
    INVALID_TOKEN(ErrorCategory.UNAUTHORIZED, "AUTH-1001", "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(ErrorCategory.UNAUTHORIZED, "AUTH-1002", "만료된 토큰입니다."),
    FAIL_SIGNIN(ErrorCategory.UNAUTHORIZED, "AUTH-1003", "로그인에 실패하였습니다. 아이디와 비밀번호를 확인해주세요."),
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
