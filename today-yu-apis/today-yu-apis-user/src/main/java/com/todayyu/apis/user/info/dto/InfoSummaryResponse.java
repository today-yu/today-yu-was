package com.todayyu.apis.user.info.dto;

import com.todayyu.application.info.dto.result.InfoSummaryResult;

import java.time.LocalDateTime;

public record InfoSummaryResponse(
        Long infoId,
        String category,
        String title,
        long helpfulMarkCount,
        LocalDateTime updatedAt
) {
    public static InfoSummaryResponse from(InfoSummaryResult info) {
        return new InfoSummaryResponse(
                info.infoId().getValue(),
                info.category().name(),
                info.title().getValue(),
                info.helpfulMarkCount(),
                info.updatedAt()
        );
    }
}
