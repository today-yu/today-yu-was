package com.todayyu.application.member.util;

import java.security.SecureRandom;

public final class VerificationCodeGenerator {
    private static final SecureRandom secureRandom = new SecureRandom();
    
    /**
     * 인증 코드를 생성하는 유틸 메서드
     *
     * <p> 문자열 변환 시 자릿수 유지를 위하여 첫 자릿수는 1 이상으로 제한한다. </p>
     *
     * @return 6자리 랜덤 숫자 문자열 인증 코드
     */
    public static String generate() {
        int code = secureRandom.nextInt(900000) + 100000;
        return String.valueOf(code);
    }
}
