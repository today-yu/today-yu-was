package com.todayyu.domain.info.vo;

import lombok.Getter;

import java.util.Objects;

@Getter
public class InfoId {
    private final Long value;
    
    public InfoId(Long value) {
        if (value == null) {
            throw new IllegalArgumentException("정보 ID는 비어있을 수 없습니다.");
        }
        
        this.value = value;
    }
    
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof InfoId infoId)) return false;
        return Objects.equals(value, infoId.value);
    }
    
    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }
}
