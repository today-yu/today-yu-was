package com.todayyu.apis.user.notice.dto.response;

import com.todayyu.application.notice.dto.result.NoticeDetailResult;

import java.time.LocalDateTime;

public record NoticeDetailResponse(
        Long id,
        String title,
        String content,
        LocalDateTime publishedAt,
        LocalDateTime updatedAt
) {
    public static NoticeDetailResponse from(NoticeDetailResult result) {
        return new NoticeDetailResponse(
                result.id().getValue(),
                result.title().getValue(),
                result.content().getValue(),
                result.publishedAt(),
                result.updatedAt()
        );
    }
}
