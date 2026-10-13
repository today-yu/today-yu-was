package com.todayyu.application.info.error;

import com.todayyu.common.error.BaseErrorType;
import com.todayyu.common.error.ErrorCategory;
import lombok.RequiredArgsConstructor;

/**
 * InfoUpdateRequest 도메인에서 비즈니스 로직 처리 중 발생하는 에러 타입
 *
 * <p> 에러 코드: {@code INFO-12xx} </p>
 */
@RequiredArgsConstructor
public enum BusinessInfoUpdateRequestErrorType implements BaseErrorType {
    NOT_FOUND(ErrorCategory.NOT_FOUND, "INFO-1201","정보 수정 요청이 존재하지 않습니다."),
    FORBIDDEN(ErrorCategory.FORBIDDEN, "INFO-1202","정보 수정 요청에 접근 권한이 없습니다."),
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
