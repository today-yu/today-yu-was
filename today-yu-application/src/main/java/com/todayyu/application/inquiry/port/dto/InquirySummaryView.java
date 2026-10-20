package com.todayyu.application.inquiry.port.dto;

import com.todayyu.domain.inquiry.vo.InquiryId;
import com.todayyu.domain.inquiry.vo.InquiryStatus;
import com.todayyu.domain.inquiry.vo.InquiryTitle;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record InquirySummaryView(
        InquiryId id,
        InquiryStatus status,
        InquiryTitle title,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
