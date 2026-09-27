package com.todayyu.application.member.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;

class VerificationCodeGeneratorTest {
    private static final int GENERATE_COUNT = 100;

    @Test
    @DisplayName("생성한 인증 코드는 첫 자리가 0이 아닌 6자리 숫자이다")
    void generateSixDigits() {
        for (int i = 0; i < GENERATE_COUNT; i++) {
            String code = VerificationCodeGenerator.generate();

            assertTrue(code.matches("[1-9]\\d{5}"), "올바르지 않은 인증 코드: " + code);
        }
    }

    @Test
    @DisplayName("인증 코드는 생성할 때마다 무작위로 생성된다")
    void generateRandomly() {
        Set<String> codes = new HashSet<>();

        for (int i = 0; i < GENERATE_COUNT; i++) {
            codes.add(VerificationCodeGenerator.generate());
        }

        assertTrue(codes.size() > 1);
    }
}
