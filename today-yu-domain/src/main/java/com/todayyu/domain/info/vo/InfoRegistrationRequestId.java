package com.todayyu.domain.info.vo;

import lombok.Getter;

import java.util.Objects;

@Getter
public class InfoRegistrationRequestId {
    private final Long value;
    
    public InfoRegistrationRequestId(Long value) {
        if (value == null) {
            throw new IllegalArgumentException("정보 등록 요청 ID는 비어있을 수 없습니다.");
        }
        
        this.value = value;
    }
    
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof InfoRegistrationRequestId that)) return false;
        return Objects.equals(value, that.value);
    }
    
    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }
}
