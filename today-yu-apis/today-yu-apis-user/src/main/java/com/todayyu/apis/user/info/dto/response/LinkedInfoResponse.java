package com.todayyu.apis.user.info.dto.response;

import com.todayyu.application.info.dto.result.LinkedInfoResult;

import java.time.LocalDateTime;

public record LinkedInfoResponse(
        Long id,
        String category,
        String title,
        LocalDateTime updatedAt
) {
    public static LinkedInfoResponse from(LinkedInfoResult result) {
        return new LinkedInfoResponse(
                result.id().getValue(),
                result.category().name(),
                result.title().getValue(),
                result.updatedAt()
        );
    }
}
