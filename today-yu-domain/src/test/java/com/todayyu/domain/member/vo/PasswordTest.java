package com.todayyu.domain.member.vo;

import com.todayyu.common.exception.CustomException;
import com.todayyu.domain.member.error.MemberErrorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PasswordTest {
    
    @ParameterizedTest
    @ValueSource(ints = {Password.MIN_LENGTH, Password.MAX_LENGTH})
    @DisplayName("최소 길이 이상, 최대 길이 이하의 비밀번호로 생성할 수 있다")
    void create(int length) {
        String value = "a".repeat(length);

        Password password = new Password(value);

        assertEquals(value, password.getValue());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    @DisplayName("비어있는 비밀번호는 생성할 수 없다")
    void blank(String value) {
        assertThrows(IllegalArgumentException.class, () -> new Password(value));
    }

    @Test
    @DisplayName("최소 길이보다 짧은 비밀번호는 생성할 수 없다")
    void tooShort() {
        String value = "a".repeat(Password.MIN_LENGTH - 1);

        CustomException exception = assertThrows(CustomException.class, () -> new Password(value));

        assertEquals(MemberErrorType.PASSWORD_TOO_SHORT, exception.getErrorType());
    }

    @Test
    @DisplayName("최대 길이보다 긴 비밀번호는 생성할 수 없다")
    void tooLong() {
        String value = "a".repeat(Password.MAX_LENGTH + 1);

        CustomException exception = assertThrows(CustomException.class, () -> new Password(value));

        assertEquals(MemberErrorType.PASSWORD_TOO_LONG, exception.getErrorType());
    }

    @Test
    @DisplayName("값이 같은 비밀번호는 같은 비밀번호이다")
    void equalsByValue() {
        Password password = new Password("password1234");

        assertEquals(new Password("password1234"), password);
        assertEquals(new Password("password1234").hashCode(), password.hashCode());
    }

    @Test
    @DisplayName("값이 다른 비밀번호는 다른 비밀번호이다")
    void notEqualsByValue() {
        assertNotEquals(new Password("password5678"), new Password("password1234"));
    }
}
