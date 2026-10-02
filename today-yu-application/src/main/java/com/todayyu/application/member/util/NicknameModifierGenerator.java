package com.todayyu.application.member.util;

import com.todayyu.domain.member.vo.NicknameModifier;

import java.util.concurrent.ThreadLocalRandom;

public final class NicknameModifierGenerator {
    private static final NicknameModifier[] MODIFIERS = NicknameModifier.values();
    
    /**
     * 닉네임 수식어({@link NicknameModifier})를 무작위로 선택하는 유틸 메서드
     *
     * @return 닉네임 수식어
     */
    public static NicknameModifier generate() {
        return MODIFIERS[ThreadLocalRandom.current().nextInt(MODIFIERS.length)];
    }
}
