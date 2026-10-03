package com.todayyu.domain.auth.vo;

import lombok.Getter;

@Getter
public class AccessToken {
    private final String value;
    
    public AccessToken(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Access Token은 비어있을 수 없습니다.");
        }
        
        this.value = value;
    }
}
