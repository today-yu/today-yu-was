package com.todayyu.domain.member.vo;

import lombok.Getter;

import java.util.Objects;

@Getter
public class EncodedPassword {
    private final String value;
    
    public EncodedPassword(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("비밀번호는 비어있을 수 없습니다.");
        }
        this.value = value;
    }
    
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof EncodedPassword encodedPassword)) return false;
        return Objects.equals(value, encodedPassword.value);
    }
    
    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }
    
    @Override
    public String toString() {
        return "[EncodedPassword]";
    }
}
