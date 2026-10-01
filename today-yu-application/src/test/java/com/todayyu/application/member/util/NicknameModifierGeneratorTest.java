package com.todayyu.application.member.util;

import com.todayyu.domain.member.vo.NicknameModifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NicknameModifierGeneratorTest {
    private static final int GENERATE_COUNT = 100;

    @Test
    @DisplayName("선택한 닉네임 수식어는 null이 아니다")
    void generateNotNull() {
        for (int i = 0; i < GENERATE_COUNT; i++) {
            assertNotNull(NicknameModifierGenerator.generate());
        }
    }

    @Test
    @DisplayName("닉네임 수식어는 선택할 때마다 무작위로 선택된다")
    void generateRandomly() {
        Set<NicknameModifier> modifiers = EnumSet.noneOf(NicknameModifier.class);

        for (int i = 0; i < GENERATE_COUNT; i++) {
            modifiers.add(NicknameModifierGenerator.generate());
        }

        assertTrue(modifiers.size() > 1);
    }
}
