package com.todayyu.domain.member.vo;

import lombok.Getter;

import java.util.Objects;

@Getter
public class Nickname {
    private final String value;
    
    private static final String BASE_NAME = "영대인";
    private static final String NICKNAME_FORMAT = "%s " + BASE_NAME + " %d";
    
    public Nickname(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("닉네임은 비어있을 수 없습니다.");
        }
        
        this.value = value;
    }
    
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Nickname nickname)) return false;
        return Objects.equals(value, nickname.value);
    }
    
    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }
    
    /**
     * 닉네임 수식어와 번호를 통해 새로운 닉네임을 생성하는 정적 팩토리 메서드
     *
     * @param modifier 닉네임 수식어
     * @param number 번호
     * @return 닉네임 객체
     */
    public static Nickname from(NicknameModifier modifier, long number) {
        if (number <= 0) {
            throw new IllegalArgumentException("닉네임 번호는 1 이상의 숫자여야합니다.");
        }
        return new Nickname(NICKNAME_FORMAT.formatted(modifier.getValue(), number));
    }
}
