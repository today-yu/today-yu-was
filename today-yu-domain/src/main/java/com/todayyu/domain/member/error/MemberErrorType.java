package com.todayyu.domain.member.error;

import com.todayyu.common.error.BaseErrorType;
import com.todayyu.common.error.ErrorCategory;
import com.todayyu.domain.member.vo.Password;
import lombok.RequiredArgsConstructor;

/**
 * Member 도메인의 불변식 위반 등으로 인해 발생하는 에러 타입
 *
 * <p> 에러 코드: {@code MEMBER-xxxx}</p>
 */
@RequiredArgsConstructor
public enum MemberErrorType implements BaseErrorType {
    VERIFICATION_CODE_REISSUE_TOO_EARLY(ErrorCategory.TOO_MANY_REQUESTS, "MEMBER-0001", "인증 코드는 발급 후 1분이 지나야 재발급할 수 있습니다."),
    EMAIL_VERIFICATION_FAIL(ErrorCategory.BUSINESS, "MEMBER-0002", "인증 코드 검증에 실패하였습니다."),
    PASSWORD_TOO_SHORT(ErrorCategory.VALIDATION, "MEMBER-0003", "비밀번호는 " + Password.MIN_LENGTH + "자 이상이어야 합니다."),
    PASSWORD_TOO_LONG(ErrorCategory.VALIDATION, "MEMBER-0004", "비밀번호는 " + Password.MAX_LENGTH + "자 이하여야 합니다."),
    EXCEED_VERIFICATION_FAIL_COUNT(ErrorCategory.TOO_MANY_REQUESTS, "MEMBER-0005", "인증 실패 횟수를 초과하였습니다."),
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
