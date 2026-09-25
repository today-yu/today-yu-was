package com.todayyu.domain.member.vo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EncodedPasswordTest {

    @Test
    @DisplayName("문자열로 변환하면 비밀번호 값 대신 [EncodedPassword]로 표시된다")
    void toStringMasksValue() {
        EncodedPassword password = new EncodedPassword("$2a$10$encodedPasswordValue");

        assertEquals("[EncodedPassword]", password.toString());
    }
}
