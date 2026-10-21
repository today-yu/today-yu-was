package com.todayyu.domain.member.vo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VerificationCodeTest {

    @Test
    @DisplayName("값으로 인증 코드를 생성할 수 있다")
    void create() {
        VerificationCode code = new VerificationCode("123456");

        assertEquals("123456", code.getValue());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    @DisplayName("비어있는 인증 코드는 생성할 수 없다")
    void blank(String value) {
        assertThrows(IllegalArgumentException.class, () -> new VerificationCode(value));
    }

    @Test
    @DisplayName("입력된 인증 코드가 일치하면 true를 반환한다")
    void verifyMatched() {
        VerificationCode code = new VerificationCode("123456");

        assertTrue(code.verify(new VerificationCode("123456")));
    }

    @Test
    @DisplayName("입력된 인증 코드가 일치하지 않으면 false를 반환한다")
    void verifyNotMatched() {
        VerificationCode code = new VerificationCode("123456");

        assertFalse(code.verify(new VerificationCode("654321")));
    }

    @Test
    @DisplayName("값이 같은 인증 코드는 같은 인증 코드이다")
    void equalsByValue() {
        VerificationCode code = new VerificationCode("123456");

        assertEquals(new VerificationCode("123456"), code);
        assertEquals(new VerificationCode("123456").hashCode(), code.hashCode());
    }

    @Test
    @DisplayName("값이 다른 인증 코드는 다른 인증 코드이다")
    void notEqualsByValue() {
        assertNotEquals(new VerificationCode("654321"), new VerificationCode("123456"));
    }
}
