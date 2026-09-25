package com.todayyu.domain.member.vo;

import lombok.Getter;

import java.util.Objects;

@Getter
public class AccountId {
    private final Long value;
    
    public AccountId(Long value) {
        if (value == null) {
            throw new IllegalArgumentException("계정 아이디는 비어있을 수 없습니다.");
        }
        
        this.value = value;
    }
    
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof AccountId accountId)) return false;
        return Objects.equals(value, accountId.value);
    }
    
    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }
}
