package com.todayyu.domain.info.vo;

import lombok.Getter;

import java.util.Objects;

@Getter
public class HelpfulMarkId {
    private final Long value;
    
    public HelpfulMarkId(Long value) {
        if (value == null) {
            throw new IllegalArgumentException("'도움이 되는 정보에요' ID는 비어있을 수 없습니다.");
        }
        
        this.value = value;
    }
    
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof HelpfulMarkId that)) return false;
        return Objects.equals(value, that.value);
    }
    
    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }
}
