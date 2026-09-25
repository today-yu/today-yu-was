package com.todayyu.domain.member.vo;

import lombok.Getter;

import java.util.Objects;

@Getter
public class AuthProviderUserId {
    private final String value;
    
    public AuthProviderUserId(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("인증 제공자에서의 사용자 아이디는 비어있을 수 없습니다.");
        }
        
        this.value = value;
    }
    
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof AuthProviderUserId that)) return false;
        return Objects.equals(value, that.value);
    }
    
    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }
}
