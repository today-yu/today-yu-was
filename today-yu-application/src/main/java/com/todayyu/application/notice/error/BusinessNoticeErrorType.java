package com.todayyu.application.notice.error;

import com.todayyu.common.error.BaseErrorType;
import com.todayyu.common.error.ErrorCategory;
import lombok.RequiredArgsConstructor;

/**
 * Notice 도메인에서 비즈니스 로직 처리 중 발생하는 에러 타입
 *
 * <p> 에러 코드: {@code: NOTICE-1xxx} </p>
 */
@RequiredArgsConstructor
public enum BusinessNoticeErrorType implements BaseErrorType {
    NOT_FOUND(ErrorCategory.NOT_FOUND, "NOTICE-1001", "존재하지 않는 공지입니다."),
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
