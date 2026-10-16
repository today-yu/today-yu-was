package com.todayyu.apis.user.info.dto.response;

import com.todayyu.application.info.dto.result.InfoUpdateRequestSummaryResult;

import java.time.LocalDateTime;

public record InfoUpdateRequestSummaryResponse(
        Long id,
        String category,
        String title,
        String status,
        LocalDateTime requestedAt
) {
    public static InfoUpdateRequestSummaryResponse from(InfoUpdateRequestSummaryResult result) {
        return new InfoUpdateRequestSummaryResponse(
                result.id().getValue(),
                result.category().name(),
                result.title().getValue(),
                result.status().name(),
                result.requestedAt()
        );
    }
}
