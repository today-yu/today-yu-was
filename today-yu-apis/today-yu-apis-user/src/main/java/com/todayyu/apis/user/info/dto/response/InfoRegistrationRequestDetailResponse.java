package com.todayyu.apis.user.info.dto.response;

import com.todayyu.application.info.dto.result.InfoRegistrationRequestDetailResult;

import java.time.LocalDateTime;

public record InfoRegistrationRequestDetailResponse(
        Long id,
        Long requesterId,
        String category,
        String content,
        String contactEmail,
        LocalDateTime requestedAt,
        String status,
        Long infoId,
        String rejectionReason
) {
    public static InfoRegistrationRequestDetailResponse from(InfoRegistrationRequestDetailResult result) {
        Long infoId = (result.infoId() == null) ? null : result.infoId().getValue();
        String rejectionReason = (result.rejectionReason() == null) ? null : result.rejectionReason().getValue();
        
        return new InfoRegistrationRequestDetailResponse(
                result.id().getValue(),
                result.requesterId().getValue(),
                result.category().name(),
                result.content().getValue(),
                result.contactEmail().getValue(),
                result.requestedAt(),
                result.status().name(),
                infoId,
                rejectionReason
        );
    }
}
