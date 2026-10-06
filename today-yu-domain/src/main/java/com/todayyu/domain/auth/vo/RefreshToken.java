package com.todayyu.domain.auth.vo;

import lombok.Getter;

import java.util.Objects;

@Getter
public class RefreshToken {
    private final String value;
    
    public RefreshToken(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Refresh Token은 비어있을 수 없습니다.");
        }
        
        this.value = value;
    }
    
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof RefreshToken that)) return false;
        return Objects.equals(value, that.value);
    }
    
    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }
}
