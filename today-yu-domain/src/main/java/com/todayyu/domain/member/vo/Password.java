package com.todayyu.domain.member.vo;

import com.todayyu.common.exception.CustomException;
import com.todayyu.domain.member.error.MemberErrorType;
import lombok.Getter;

import java.util.Objects;

@Getter
public class Password {
    public static final int MIN_LENGTH = 8;
    public static final int MAX_LENGTH = 20;
    
    private final String value;
    
    public Password(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("비밀번호는 비어있을 수 없습니다.");
        }
        
        if (value.length() < MIN_LENGTH) {
            throw new CustomException(MemberErrorType.PASSWORD_TOO_SHORT);
        }
        
        if (value.length() > MAX_LENGTH) {
            throw new CustomException(MemberErrorType.PASSWORD_TOO_LONG);
        }
        
        this.value = value;
    }
    
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Password password)) return false;
        return Objects.equals(value, password.value);
    }
    
    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }
}
