package com.todayyu.domain.member.vo;

import lombok.Getter;

import java.util.Objects;

@Getter
public class MemberId {
    private final Long value;
    
    public MemberId(Long value) {
        if (value == null) {
            throw new IllegalArgumentException("회원 아이디는 비어있을 수 없습니다.");
        }
        
        this.value = value;
    }
    
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof MemberId memberId)) return false;
        return Objects.equals(value, memberId.value);
    }
    
    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }
}
