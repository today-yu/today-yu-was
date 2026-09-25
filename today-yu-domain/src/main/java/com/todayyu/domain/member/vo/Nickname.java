package com.todayyu.domain.member.vo;

import lombok.Getter;

import java.util.Objects;

@Getter
public class Nickname {
    private final String value;
    
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
}
