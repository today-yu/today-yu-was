package com.todayyu.domain.member.vo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VerificationFailCountTest {

    @Test
    @DisplayName("0으로 인증 실패 횟수를 생성할 수 있다")
    void zero() {
        VerificationFailCount failCount = VerificationFailCount.zero();

        assertEquals(0, failCount.getValue());
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 5})
    @DisplayName("0 이상의 값으로 인증 실패 횟수를 생성할 수 있다")
    void of(int value) {
        VerificationFailCount failCount = VerificationFailCount.of(value);

        assertEquals(value, failCount.getValue());
    }

    @Test
    @DisplayName("값 없이 인증 실패 횟수를 생성할 수 없다")
    void createWithoutValue() {
        assertThrows(IllegalArgumentException.class, () -> VerificationFailCount.of(null));
    }

    @Test
    @DisplayName("음수로 인증 실패 횟수를 생성할 수 없다")
    void negative() {
        assertThrows(IllegalArgumentException.class, () -> VerificationFailCount.of(-1));
    }

    @Test
    @DisplayName("인증 실패 횟수를 증가시키면 1 증가한 새로운 인증 실패 횟수를 반환한다")
    void increase() {
        VerificationFailCount failCount = VerificationFailCount.of(2);

        VerificationFailCount increased = failCount.increase();

        assertEquals(3, increased.getValue());
        assertEquals(2, failCount.getValue());
    }

    @ParameterizedTest
    @ValueSource(ints = {5, 6})
    @DisplayName("인증 실패 횟수가 대상 횟수 이상이면 도달한 것이다")
    void reached(int value) {
        VerificationFailCount failCount = VerificationFailCount.of(value);

        assertTrue(failCount.isReached(5));
    }

    @Test
    @DisplayName("인증 실패 횟수가 대상 횟수 미만이면 도달하지 않은 것이다")
    void notReached() {
        VerificationFailCount failCount = VerificationFailCount.of(4);

        assertFalse(failCount.isReached(5));
    }

    @Test
    @DisplayName("값이 같은 인증 실패 횟수는 같은 인증 실패 횟수이다")
    void equalsByValue() {
        assertEquals(VerificationFailCount.of(1), VerificationFailCount.of(1));
        assertEquals(VerificationFailCount.of(1).hashCode(), VerificationFailCount.of(1).hashCode());
    }

    @Test
    @DisplayName("값이 다른 인증 실패 횟수는 다른 인증 실패 횟수이다")
    void notEqualsByValue() {
        assertNotEquals(VerificationFailCount.of(1), VerificationFailCount.of(2));
    }
}
