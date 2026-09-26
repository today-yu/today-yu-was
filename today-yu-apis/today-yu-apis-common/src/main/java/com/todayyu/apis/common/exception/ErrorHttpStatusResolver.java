package com.todayyu.apis.common.exception;

import com.todayyu.common.error.ErrorCategory;
import org.springframework.http.HttpStatus;

public final class ErrorHttpStatusResolver {
    
    private ErrorHttpStatusResolver() { }
    
    /**
     * {@link ErrorCategory}를 {@link HttpStatus}로 변환하는 유틸 메서드
     *
     * @param category {@link ErrorCategory} 객체
     * @return 에러 카테고리를 HTTP 상태 코드에 맞게 변환한 {@link HttpStatus} 객체
     */
    public static HttpStatus resolve(ErrorCategory category) {
        return switch (category) {
            case BUSINESS -> HttpStatus.BAD_REQUEST;
            case UNAUTHORIZED -> HttpStatus.UNAUTHORIZED;
            case FORBIDDEN -> HttpStatus.FORBIDDEN;
            case NOT_FOUND -> HttpStatus.NOT_FOUND;
            case CONFLICT -> HttpStatus.CONFLICT;
            case VALIDATION -> HttpStatus.UNPROCESSABLE_CONTENT;
            case TOO_MANY_REQUESTS -> HttpStatus.TOO_MANY_REQUESTS;
            case SERVER -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }
}
