package com.todayyu.domain.member.entity;

import com.todayyu.common.exception.CustomException;
import com.todayyu.domain.common.vo.Email;
import com.todayyu.domain.member.error.MemberErrorType;
import com.todayyu.domain.member.vo.VerificationCode;
import com.todayyu.domain.member.vo.VerificationFailCount;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PasswordResetVerificationTest {
    private static final Email EMAIL = new Email("user@yu.ac.kr");
    private static final VerificationCode CODE = new VerificationCode("123456");
    private static final VerificationCode WRONG_CODE = new VerificationCode("654321");
    private static final VerificationCode NEW_CODE = new VerificationCode("111111");
    private static final LocalDateTime ISSUED_AT = LocalDateTime.of(2026, 1, 1, 12, 0);
    private static final int MAX_FAIL_COUNT = 5;

    @Test
    @DisplayName("이메일, 인증 코드, 발급 시각으로 실패 횟수가 0인 비밀번호 초기화 인증을 발급할 수 있다")
    void issue() {
        PasswordResetVerification verification = PasswordResetVerification.issue(EMAIL, CODE, ISSUED_AT);

        assertEquals(EMAIL, verification.getEmail());
        assertEquals(CODE, verification.getCode());
        assertEquals(ISSUED_AT, verification.getIssuedAt());
        assertEquals(VerificationFailCount.zero(), verification.getFailCount());
    }

    @Test
    @DisplayName("저장된 실패 횟수로 비밀번호 초기화 인증을 복원할 수 있다")
    void of() {
        VerificationFailCount failCount = VerificationFailCount.of(3);

        PasswordResetVerification verification = PasswordResetVerification.of(EMAIL, CODE, ISSUED_AT, failCount);

        assertEquals(EMAIL, verification.getEmail());
        assertEquals(CODE, verification.getCode());
        assertEquals(ISSUED_AT, verification.getIssuedAt());
        assertEquals(failCount, verification.getFailCount());
    }

    @Test
    @DisplayName("이메일 없이 비밀번호 초기화 인증을 생성할 수 없다")
    void createWithoutEmail() {
        assertThrows(IllegalArgumentException.class,
                () -> PasswordResetVerification.issue(null, CODE, ISSUED_AT));
    }

    @Test
    @DisplayName("인증 코드 없이 비밀번호 초기화 인증을 생성할 수 없다")
    void createWithoutCode() {
        assertThrows(IllegalArgumentException.class,
                () -> PasswordResetVerification.issue(EMAIL, null, ISSUED_AT));
    }

    @Test
    @DisplayName("발급 시각 없이 비밀번호 초기화 인증을 생성할 수 없다")
    void createWithoutIssuedAt() {
        assertThrows(IllegalArgumentException.class,
                () -> PasswordResetVerification.issue(EMAIL, CODE, null));
    }

    @Test
    @DisplayName("실패 횟수 없이 비밀번호 초기화 인증을 생성할 수 없다")
    void createWithoutFailCount() {
        assertThrows(IllegalArgumentException.class,
                () -> PasswordResetVerification.of(EMAIL, CODE, ISSUED_AT, null));
    }

    @Test
    @DisplayName("이메일이 같은 비밀번호 초기화 인증은 같은 인증이다")
    void equalsByEmail() {
        PasswordResetVerification verification = PasswordResetVerification.issue(EMAIL, CODE, ISSUED_AT);
        PasswordResetVerification other = PasswordResetVerification.of(
                EMAIL, NEW_CODE, ISSUED_AT.plusMinutes(1), VerificationFailCount.of(2)
        );

        assertEquals(verification, other);
        assertEquals(verification.hashCode(), other.hashCode());
    }

    @Test
    @DisplayName("이메일이 다른 비밀번호 초기화 인증은 다른 인증이다")
    void notEqualsByEmail() {
        PasswordResetVerification verification = PasswordResetVerification.issue(EMAIL, CODE, ISSUED_AT);
        PasswordResetVerification other = PasswordResetVerification.issue(new Email("other@yu.ac.kr"), CODE, ISSUED_AT);

        assertNotEquals(verification, other);
    }

    @Test
    @DisplayName("인증 코드가 일치하면 true를 반환하고 실패 횟수는 그대로이다")
    void verifyMatched() {
        PasswordResetVerification verification = PasswordResetVerification.of(
                EMAIL, CODE, ISSUED_AT, VerificationFailCount.of(2)
        );

        boolean matched = verification.verify(CODE);

        assertTrue(matched);
        assertEquals(VerificationFailCount.of(2), verification.getFailCount());
    }

    @Test
    @DisplayName("인증 코드가 일치하지 않으면 false를 반환하고 실패 횟수가 1 증가한다")
    void verifyNotMatched() {
        PasswordResetVerification verification = PasswordResetVerification.issue(EMAIL, CODE, ISSUED_AT);

        boolean matched = verification.verify(WRONG_CODE);

        assertFalse(matched);
        assertEquals(VerificationFailCount.of(1), verification.getFailCount());
    }

    @Test
    @DisplayName("최대 실패 횟수 직전까지는 인증 코드를 검증할 수 있다")
    void verifyBeforeReachedMaxFailCount() {
        PasswordResetVerification verification = PasswordResetVerification.of(
                EMAIL, CODE, ISSUED_AT, VerificationFailCount.of(MAX_FAIL_COUNT - 1)
        );

        assertFalse(verification.verify(WRONG_CODE));
        assertEquals(VerificationFailCount.of(MAX_FAIL_COUNT), verification.getFailCount());
    }

    @Test
    @DisplayName("최대 실패 횟수에 도달한 인증은 일치하는 인증 코드라도 검증할 수 없다")
    void verifyAfterReachedMaxFailCount() {
        PasswordResetVerification verification = PasswordResetVerification.of(
                EMAIL, CODE, ISSUED_AT, VerificationFailCount.of(MAX_FAIL_COUNT)
        );

        CustomException exception = assertThrows(CustomException.class, () -> verification.verify(CODE));

        assertEquals(MemberErrorType.EXCEED_VERIFICATION_FAIL_COUNT, exception.getErrorType());
        assertEquals(VerificationFailCount.of(MAX_FAIL_COUNT), verification.getFailCount());
    }

    @Test
    @DisplayName("최대 실패 횟수만큼 인증에 실패하면 이후 검증할 수 없다")
    void verifyRepeatedlyFailed() {
        PasswordResetVerification verification = PasswordResetVerification.issue(EMAIL, CODE, ISSUED_AT);

        for (int i = 0; i < MAX_FAIL_COUNT; i++) {
            assertFalse(verification.verify(WRONG_CODE));
        }

        CustomException exception = assertThrows(CustomException.class, () -> verification.verify(CODE));

        assertEquals(MemberErrorType.EXCEED_VERIFICATION_FAIL_COUNT, exception.getErrorType());
    }

    @Test
    @DisplayName("만료 시각은 발급 시각으로부터 5분 뒤이다")
    void expiredAt() {
        PasswordResetVerification verification = PasswordResetVerification.issue(EMAIL, CODE, ISSUED_AT);

        assertEquals(ISSUED_AT.plusMinutes(5), verification.getExpiredAt());
    }

    @Test
    @DisplayName("발급 후 1분이 지나면 새로운 인증 코드와 재발급 시각으로 재발급할 수 있다")
    void reissue() {
        PasswordResetVerification verification = PasswordResetVerification.issue(EMAIL, CODE, ISSUED_AT);
        LocalDateTime now = ISSUED_AT.plusMinutes(1);

        PasswordResetVerification reissued = verification.reissue(NEW_CODE, now);

        assertEquals(EMAIL, reissued.getEmail());
        assertEquals(NEW_CODE, reissued.getCode());
        assertEquals(now, reissued.getIssuedAt());
    }

    @Test
    @DisplayName("재발급하면 실패 횟수가 0으로 초기화된다")
    void reissueResetsFailCount() {
        PasswordResetVerification verification = PasswordResetVerification.of(
                EMAIL, CODE, ISSUED_AT, VerificationFailCount.of(MAX_FAIL_COUNT)
        );

        PasswordResetVerification reissued = verification.reissue(NEW_CODE, ISSUED_AT.plusMinutes(1));

        assertEquals(VerificationFailCount.zero(), reissued.getFailCount());
        assertTrue(reissued.verify(NEW_CODE));
    }

    @Test
    @DisplayName("발급 후 1분이 지나기 전에는 재발급할 수 없다")
    void reissueTooEarly() {
        PasswordResetVerification verification = PasswordResetVerification.issue(EMAIL, CODE, ISSUED_AT);
        LocalDateTime now = ISSUED_AT.plusMinutes(1).minusSeconds(1);

        CustomException exception = assertThrows(CustomException.class, () -> verification.reissue(NEW_CODE, now));

        assertEquals(MemberErrorType.VERIFICATION_CODE_REISSUE_TOO_EARLY, exception.getErrorType());
    }
}
