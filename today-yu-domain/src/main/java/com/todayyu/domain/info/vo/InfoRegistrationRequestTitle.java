package com.todayyu.domain.info.vo;

import lombok.Getter;

import java.util.Objects;

@Getter
public class InfoRegistrationRequestTitle {
    private final String value;
    
    public InfoRegistrationRequestTitle(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("정보 등록 요청 제목은 비어있을 수 없습니다.");
        }
        
        this.value = value;
    }
    
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof InfoRegistrationRequestTitle that)) return false;
        return Objects.equals(value, that.value);
    }
    
    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }
}
