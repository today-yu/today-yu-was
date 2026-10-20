package com.todayyu.apis.user.inquiry.dto.response;

import com.todayyu.application.inquiry.dto.result.InquiryDetailResult;

import java.time.LocalDateTime;

public record InquiryDetailResponse(
        Long id,
        Long memberId,
        String contactEmail,
        String status,
        String title,
        String content,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime answeredAt
) {
    public static InquiryDetailResponse from(InquiryDetailResult result) {
        return new InquiryDetailResponse(
                result.id().getValue(),
                result.memberId().getValue(),
                result.contactEmail().getValue(),
                result.status().name(),
                result.title().getValue(),
                result.content().getValue(),
                result.createdAt(),
                result.updatedAt(),
                result.answeredAt()
        );
    }
}
