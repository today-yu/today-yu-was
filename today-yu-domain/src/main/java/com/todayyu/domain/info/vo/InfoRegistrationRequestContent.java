package com.todayyu.domain.info.vo;

import lombok.Getter;

import java.util.Objects;

@Getter
public class InfoRegistrationRequestContent {
    private final String value;
    
    public InfoRegistrationRequestContent(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("정보 등록 요청 내용은 비어있을 수 없습니다.");
        }
        
        this.value = value;
    }
    
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof InfoRegistrationRequestContent that)) return false;
        return Objects.equals(value, that.value);
    }
    
    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }
}
