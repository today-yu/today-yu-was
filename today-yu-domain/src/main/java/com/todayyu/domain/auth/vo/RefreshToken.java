package com.todayyu.domain.auth.vo;

import lombok.Getter;

@Getter
public class RefreshToken {
    private final String value;
    
    public RefreshToken(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Refresh Token은 비어있을 수 없습니다.");
        }
        
        this.value = value;
    }
}
