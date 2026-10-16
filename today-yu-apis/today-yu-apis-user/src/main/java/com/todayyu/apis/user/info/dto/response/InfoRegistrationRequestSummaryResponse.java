package com.todayyu.apis.user.info.dto.response;

import com.todayyu.application.info.dto.result.InfoRegistrationRequestSummaryResult;

import java.time.LocalDateTime;

public record InfoRegistrationRequestSummaryResponse(
        Long id,
        String category,
        String title,
        String status,
        LocalDateTime requestedAt
) {
    public static InfoRegistrationRequestSummaryResponse from(InfoRegistrationRequestSummaryResult result) {
        return new InfoRegistrationRequestSummaryResponse(
                result.id().getValue(),
                result.category().name(),
                result.title().getValue(),
                result.status().name(),
                result.requestedAt()
        );
    }
}
