package com.todayyu.domain.info.vo;

import lombok.Getter;

import java.util.Objects;

@Getter
public class InfoUpdateRequestContent {
    private final String value;
    
    public InfoUpdateRequestContent(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("정보 수정 요청 내용은 비어있을 수 없습니다.");
        }
        
        this.value = value;
    }
    
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof InfoUpdateRequestContent that)) return false;
        return Objects.equals(value, that.value);
    }
    
    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }
}
