package com.todayyu.domain.inquiry.vo;

import lombok.Getter;

import java.util.Objects;

@Getter
public class InquiryTitle {
    private final String value;
    
    public InquiryTitle(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("문의 제목은 비어있을 수 없습니다.");
        }
        
        this.value = value;
    }
    
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof InquiryTitle that)) return false;
        return Objects.equals(value, that.value);
    }
    
    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }
}
