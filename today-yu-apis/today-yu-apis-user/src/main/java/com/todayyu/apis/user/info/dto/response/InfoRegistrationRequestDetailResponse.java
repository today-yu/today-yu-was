package com.todayyu.apis.user.info.dto.response;

import com.todayyu.application.info.dto.result.InfoRegistrationRequestDetailResult;

import java.time.LocalDateTime;

public record InfoRegistrationRequestDetailResponse(
        Long id,
        Long requesterId,
        String category,
        String title,
        String content,
        String contactEmail,
        LocalDateTime requestedAt,
        String status,
        LinkedInfoResponse info,
        String rejectionReason
) {
    public static InfoRegistrationRequestDetailResponse from(InfoRegistrationRequestDetailResult result) {
        LinkedInfoResponse linkedInfoResponse = (result.info() == null) ? null : LinkedInfoResponse.from(result.info());
        String rejectionReason = (result.rejectionReason() == null) ? null : result.rejectionReason().getValue();
        
        return new InfoRegistrationRequestDetailResponse(
                result.id().getValue(),
                result.requesterId().getValue(),
                result.category().name(),
                result.title().getValue(),
                result.content().getValue(),
                result.contactEmail().getValue(),
                result.requestedAt(),
                result.status().name(),
                linkedInfoResponse,
                rejectionReason
        );
    }
}
