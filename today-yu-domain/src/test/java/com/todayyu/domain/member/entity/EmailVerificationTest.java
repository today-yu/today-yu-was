package com.todayyu.domain.member.entity;

import com.todayyu.domain.common.vo.Email;
import com.todayyu.domain.member.vo.VerificationCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EmailVerificationTest {
    private static final Email EMAIL = new Email("user@yu.ac.kr");
    private static final VerificationCode CODE = new VerificationCode("123456");
    private static final LocalDateTime ISSUED_AT = LocalDateTime.of(2026, 1, 1, 12, 0);

    @Test
    @DisplayName("이메일, 인증 코드, 발급 시각으로 이메일 인증을 생성할 수 있다")
    void create() {
        EmailVerification verification = new EmailVerification(EMAIL, CODE, ISSUED_AT);

        assertEquals(EMAIL, verification.getEmail());
        assertEquals(CODE, verification.getCode());
        assertEquals(ISSUED_AT, verification.getIssuedAt());
    }

    @Test
    @DisplayName("이메일 없이 이메일 인증을 생성할 수 없다")
    void createWithoutEmail() {
        assertThrows(IllegalArgumentException.class,
                () -> new EmailVerification(null, CODE, ISSUED_AT));
    }

    @Test
    @DisplayName("인증 코드 없이 이메일 인증을 생성할 수 없다")
    void createWithoutCode() {
        assertThrows(IllegalArgumentException.class,
                () -> new EmailVerification(EMAIL, null, ISSUED_AT));
    }

    @Test
    @DisplayName("발급 시각 없이 이메일 인증을 생성할 수 없다")
    void createWithoutIssuedAt() {
        assertThrows(IllegalArgumentException.class,
                () -> new EmailVerification(EMAIL, CODE, null));
    }

    @Test
    @DisplayName("입력된 인증 코드가 일치하면 true를 반환한다")
    void verifyMatched() {
        EmailVerification verification = new EmailVerification(EMAIL, CODE, ISSUED_AT);

        assertTrue(verification.verify(new VerificationCode("123456")));
    }

    @Test
    @DisplayName("입력된 인증 코드가 일치하지 않으면 false를 반환한다")
    void verifyNotMatched() {
        EmailVerification verification = new EmailVerification(EMAIL, CODE, ISSUED_AT);

        assertFalse(verification.verify(new VerificationCode("654321")));
    }
}
