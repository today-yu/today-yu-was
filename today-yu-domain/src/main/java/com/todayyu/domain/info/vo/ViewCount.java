package com.todayyu.domain.info.vo;

import lombok.Getter;

import java.util.Objects;

@Getter
public class ViewCount {
    private final Long value;
    
    public ViewCount(Long value) {
        if (value == null) {
            throw new IllegalArgumentException("조회수는 비어있을 수 없습니다.");
        }
        
        if (value < 0) {
            throw new IllegalArgumentException("조회수는 0 이상이어야 합니다.");
        }
        
        this.value = value;
    }
    
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ViewCount viewCount)) return false;
        return Objects.equals(value, viewCount.value);
    }
    
    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }
}
