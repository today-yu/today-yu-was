package com.todayyu.apis.user.inquiry.dto.response;

import com.todayyu.application.inquiry.dto.result.InquirySummaryResult;

import java.time.LocalDateTime;

public record InquirySummaryResponse(
        Long id,
        String status,
        String title,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static InquirySummaryResponse from(InquirySummaryResult result) {
        return new InquirySummaryResponse(
                result.id().getValue(),
                result.status().name(),
                result.title().getValue(),
                result.createdAt(),
                result.updatedAt()
        );
    }
}
