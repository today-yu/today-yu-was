package com.todayyu.domain.info.vo;

import lombok.Getter;

import java.util.Objects;

@Getter
public class RejectionReason {
    private final String value;
    
    public RejectionReason(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("반려 사유는 비어있을 수 없습니다.");
        }
        
        this.value = value;
    }
    
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof RejectionReason that)) return false;
        return Objects.equals(value, that.value);
    }
    
    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }
}
