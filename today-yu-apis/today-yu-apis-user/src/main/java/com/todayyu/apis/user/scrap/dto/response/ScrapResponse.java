package com.todayyu.apis.user.scrap.dto.response;

import com.todayyu.application.scrap.dto.result.ScrapResult;

import java.time.LocalDateTime;

public record ScrapResponse(
        Long infoId,
        String category,
        String title,
        long helpfulMarkCount,
        LocalDateTime updatedAt
) {
    public static ScrapResponse from(ScrapResult result) {
        return new ScrapResponse(
                result.infoId().getValue(),
                result.category().name(),
                result.title().getValue(),
                result.helpfulMarkCount(),
                result.updatedAt()
        );
    }
}
