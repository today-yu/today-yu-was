package com.todayyu.domain.inquiry.vo;

import lombok.Getter;

import java.util.Objects;

@Getter
public class InquiryId {
    private final Long value;
    
    public InquiryId(Long value) {
        if (value == null) {
            throw new IllegalArgumentException("문의 ID는 비어있을 수 없습니다.");
        }
        
        this.value = value;
    }
    
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof InquiryId inquiryId)) return false;
        return Objects.equals(value, inquiryId.value);
    }
    
    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }
}
