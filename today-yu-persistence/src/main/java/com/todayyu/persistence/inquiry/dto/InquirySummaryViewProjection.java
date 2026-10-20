package com.todayyu.persistence.inquiry.dto;

import com.todayyu.application.inquiry.port.dto.InquirySummaryView;
import com.todayyu.domain.inquiry.vo.InquiryId;
import com.todayyu.domain.inquiry.vo.InquiryStatus;
import com.todayyu.domain.inquiry.vo.InquiryTitle;

import java.time.LocalDateTime;

public record InquirySummaryViewProjection(
        Long id,
        InquiryStatus status,
        String title,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public InquirySummaryView toView() {
        return InquirySummaryView.builder()
                .id(new InquiryId(id))
                .status(status)
                .title(new InquiryTitle(title))
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }
}
