package com.todayyu.application.inquiry.dto.result;

import com.todayyu.application.inquiry.port.dto.InquirySummaryView;
import com.todayyu.domain.inquiry.vo.InquiryId;
import com.todayyu.domain.inquiry.vo.InquiryStatus;
import com.todayyu.domain.inquiry.vo.InquiryTitle;

import java.time.LocalDateTime;

public record InquirySummaryResult(
        InquiryId id,
        InquiryStatus status,
        InquiryTitle title,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static InquirySummaryResult from(InquirySummaryView view) {
        return new InquirySummaryResult(
                view.id(),
                view.status(),
                view.title(),
                view.createdAt(),
                view.updatedAt()
        );
    }
}
