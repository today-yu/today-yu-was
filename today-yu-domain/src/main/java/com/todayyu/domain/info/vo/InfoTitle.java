package com.todayyu.domain.info.vo;

import lombok.Getter;

import java.util.Objects;

@Getter
public class InfoTitle {
    private final String value;
    
    public InfoTitle(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("정보 제목은 비어있을 수 없습니다.");
        }
        
        this.value = value;
    }
    
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof InfoTitle infoTitle)) return false;
        return Objects.equals(value, infoTitle.value);
    }
    
    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }
}
