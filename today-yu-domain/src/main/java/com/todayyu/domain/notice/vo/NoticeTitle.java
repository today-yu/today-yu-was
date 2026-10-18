package com.todayyu.domain.notice.vo;

import lombok.Getter;

import java.util.Objects;

@Getter
public class NoticeTitle {
    private final String value;
    
    public NoticeTitle(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("공지 제목은 비어있을 수 없습니다.");
        }
        
        this.value = value;
    }
    
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof NoticeTitle that)) return false;
        return Objects.equals(value, that.value);
    }
    
    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }
}
