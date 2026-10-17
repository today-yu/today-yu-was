package com.todayyu.domain.scrap.vo;

import lombok.Getter;

import java.util.Objects;

@Getter
public class ScrapId {
    private final Long value;
    
    public ScrapId(Long value) {
        if (value == null) {
            throw new IllegalArgumentException("스크랩 ID는 비어있을 수 없습니다.");
        }
        
        this.value = value;
    }
    
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ScrapId scrapId)) return false;
        return Objects.equals(value, scrapId.value);
    }
    
    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }
}
