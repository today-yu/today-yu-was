package com.todayyu.domain.member.vo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EmailTest {

    @ParameterizedTest
    @ValueSource(strings = {"user@yu.ac.kr", "user.name+tag@gmail.com", "a_b-c@sub.example.co.kr", "User@YU.ac.KR"})
    @DisplayName("올바른 형식의 이메일로 생성할 수 있다")
    void create(String value) {
        Email email = new Email(value);

        assertEquals(value, email.getValue());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    @DisplayName("비어있는 이메일은 생성할 수 없다")
    void blank(String value) {
        assertThrows(IllegalArgumentException.class, () -> new Email(value));
    }

    @ParameterizedTest
    @ValueSource(strings = {"user", "user@", "@yu.ac.kr", "user@yu", "user@yu..ac.kr", "user@yu.ac.k", "us er@yu.ac.kr"})
    @DisplayName("형식이 올바르지 않은 이메일은 생성할 수 없다")
    void invalidFormat(String value) {
        assertThrows(IllegalArgumentException.class, () -> new Email(value));
    }

    @Test
    @DisplayName("값이 같은 이메일은 같은 이메일이다")
    void equalsByValue() {
        Email email = new Email("user@yu.ac.kr");

        assertEquals(new Email("user@yu.ac.kr"), email);
        assertEquals(new Email("user@yu.ac.kr").hashCode(), email.hashCode());
    }

    @Test
    @DisplayName("값이 다른 이메일은 다른 이메일이다")
    void notEqualsByValue() {
        assertNotEquals(new Email("other@yu.ac.kr"), new Email("user@yu.ac.kr"));
    }
}
