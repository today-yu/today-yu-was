package com.todayyu.domain.inquiry.vo;

import lombok.Getter;

import java.util.Objects;

@Getter
public class InquiryContent {
    private final String value;
    
    public InquiryContent(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("문의 내용은 비어있을 수 없습니다.");
        }
        
        this.value = value;
    }
    
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof InquiryContent that)) return false;
        return Objects.equals(value, that.value);
    }
    
    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }
}
