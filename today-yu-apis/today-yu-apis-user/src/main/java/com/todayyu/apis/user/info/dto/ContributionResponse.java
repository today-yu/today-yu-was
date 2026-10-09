package com.todayyu.apis.user.info.dto;

import com.todayyu.application.info.dto.ContributionResult;
import com.todayyu.application.info.dto.ContributionType;

import java.time.LocalDateTime;

public record ContributionResponse(
        Long memberId,
        String nickname,
        ContributionType type,
        Long requestId,
        LocalDateTime requestedAt
) {
    public static ContributionResponse from(ContributionResult result) {
        return new ContributionResponse(
                result.memberId().getValue(),
                result.nickname().getValue(),
                result.type(),
                result.requestId(),
                result.requestedAt()
        );
    }
}
