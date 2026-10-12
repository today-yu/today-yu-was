package com.todayyu.apis.user.info.dto;

import com.todayyu.application.info.dto.InfoDetailResult;

import java.time.LocalDateTime;
import java.util.List;

public record InfoDetailResponse(
        Long id,
        String category,
        String title,
        String content,
        LocalDateTime publishedAt,
        LocalDateTime updatedAt,
        long viewCount,
        long helpfulMarkCount,
        boolean helpfulMarked,
        List<ContributionResponse> contributions
) {
    public static InfoDetailResponse from(InfoDetailResult result) {
        return new InfoDetailResponse(
                result.id().getValue(),
                result.category().name(),
                result.title().getValue(),
                result.content().getValue(),
                result.publishedAt(),
                result.updatedAt(),
                result.viewCount().getValue(),
                result.helpfulMarkCount(),
                result.helpfulMarked(),
                result.contributions().stream().map(ContributionResponse::from).toList()
        );
    }
}
