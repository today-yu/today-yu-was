package com.todayyu.common.error;

public interface BaseErrorType {
    /**
     * {@link ErrorCategory}를 반환하는 메서드
     *
     * @return 에러 카테고리 객체
     */
    ErrorCategory getCategory();
    
    /**
     * 에러 코드를 반환하는 메서드
     *
     * <p> 에러 코드는 {@code {DOMAIN(대문자)}-{4자리 숫자}}로 구성된다.
     *
     * @return 에러 코드 {@code {DOMAIN(대문자)}-{4자리 숫자}}
     */
    String getErrorCode();
    
    /**
     * 에러 메시지를 반환하는 메서드
     *
     * @return 에러 메시지
     */
    String getMessage();
}
