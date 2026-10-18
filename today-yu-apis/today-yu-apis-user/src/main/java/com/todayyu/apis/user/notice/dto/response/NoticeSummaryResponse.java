package com.todayyu.apis.user.notice.dto.response;

import com.todayyu.application.notice.dto.result.NoticeSummaryResult;

import java.time.LocalDateTime;

public record NoticeSummaryResponse(
        Long id,
        String title,
        LocalDateTime publishedAt
) {
    public static NoticeSummaryResponse from(NoticeSummaryResult result) {
        return new NoticeSummaryResponse(
                result.id().getValue(),
                result.title().getValue(),
                result.publishedAt()
        );
    }
}
