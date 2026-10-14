package com.todayyu.apis.user.info.dto.response;

import com.todayyu.application.info.dto.result.InfoUpdateRequestDetailResult;

import java.time.LocalDateTime;

public record InfoUpdateRequestDetailResponse(
        Long id,
        Long requesterId,
        String content,
        String contactEmail,
        LocalDateTime observedAt,
        LocalDateTime requestedAt,
        String status,
        LinkedInfoResponse info,
        String rejectionReason
) {
    public static InfoUpdateRequestDetailResponse from(InfoUpdateRequestDetailResult result) {
        LinkedInfoResponse linkedInfoResponse =
                (result.info() == null) ? null : LinkedInfoResponse.from(result.info());
        
        String rejectionReason
                = (result.rejectionReason() == null) ? null : result.rejectionReason().getValue();
        
        return new InfoUpdateRequestDetailResponse(
                result.id().getValue(),
                result.requesterId().getValue(),
                result.content().getValue(),
                result.contactEmail().getValue(),
                result.observedAt(),
                result.requestedAt(),
                result.status().name(),
                linkedInfoResponse,
                rejectionReason
        );
    }
}
