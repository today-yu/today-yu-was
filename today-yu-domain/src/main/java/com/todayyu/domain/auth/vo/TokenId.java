package com.todayyu.domain.auth.vo;

import lombok.Getter;

@Getter
public class TokenId {
    private final String value;
    
    public TokenId(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("토큰 ID는 비어있을 수 없습니다.");
        }
        
        this.value = value;
    }
}
