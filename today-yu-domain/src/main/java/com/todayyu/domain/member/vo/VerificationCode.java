package com.todayyu.domain.member.vo;

import lombok.Getter;

import java.util.Objects;

@Getter
public class VerificationCode {
    private final String value;
    
    public VerificationCode(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("이메일 인증 코드는 비어있을 수 없습니다.");
        }
        
        this.value = value;
    }
    
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof VerificationCode that)) return false;
        return Objects.equals(value, that.value);
    }
    
    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }
}
