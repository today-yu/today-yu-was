package com.todayyu.application.inquiry.dto;

import com.todayyu.domain.inquiry.vo.InquiryId;

public record InquiryCursor(
        InquiryId id
) {
    public InquiryCursor {
        if (id == null) {
            throw new IllegalArgumentException("커서 값은 비어있을 수 없습니다.");
        }
    }
}
