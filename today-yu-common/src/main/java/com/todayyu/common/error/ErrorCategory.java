package com.todayyu.common.error;

public enum ErrorCategory {
    BUSINESS,           // 비즈니스 규칙
    UNAUTHORIZED,       // 인증
    FORBIDDEN,          // 인가
    NOT_FOUND,          // 존재하지 않는 리소스
    CONFLICT,           // 충돌, 경합, 상태 충돌
    VALIDATION,         // 유효성 검증
    TOO_MANY_REQUESTS,  // 많은 요청
    SERVER,             // 서버 내 에러 (알 수 없는 에러 및 인프라 예외)
}
