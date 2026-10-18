package com.todayyu.domain.notice.vo;

import lombok.Getter;

import java.util.Objects;

@Getter
public class NoticeId {
    private final Long value;
    
    public NoticeId(Long value) {
        if (value == null) {
            throw new IllegalArgumentException("공지 ID는 비어있을 수 없습니다.");
        }
        
        this.value = value;
    }
    
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof NoticeId noticeId)) return false;
        return Objects.equals(value, noticeId.value);
    }
    
    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }
}
