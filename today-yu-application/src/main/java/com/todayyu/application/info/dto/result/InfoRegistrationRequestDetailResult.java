package com.todayyu.application.info.dto.result;

import com.todayyu.domain.common.vo.Email;
import com.todayyu.domain.info.entity.InfoRegistrationRequest;
import com.todayyu.domain.info.vo.InfoCategory;
import com.todayyu.domain.info.vo.InfoId;
import com.todayyu.domain.info.vo.InfoRegistrationRequestContent;
import com.todayyu.domain.info.vo.InfoRegistrationRequestId;
import com.todayyu.domain.info.vo.InfoRequestStatus;
import com.todayyu.domain.info.vo.RejectionReason;
import com.todayyu.domain.member.vo.MemberId;

import java.time.LocalDateTime;

public record InfoRegistrationRequestDetailResult(
        InfoRegistrationRequestId id,
        MemberId requesterId,
        InfoCategory category,
        InfoRegistrationRequestContent content,
        Email contactEmail,
        LocalDateTime requestedAt,
        InfoRequestStatus status,
        InfoId infoId,
        RejectionReason rejectionReason
) {
    public static InfoRegistrationRequestDetailResult from(InfoRegistrationRequest request) {
        return new InfoRegistrationRequestDetailResult(
                request.getId(),
                request.getRequesterId(),
                request.getCategory(),
                request.getContent(),
                request.getContactEmail(),
                request.getRequestedAt(),
                request.getStatus(),
                request.getInfoId(),
                request.getRejectionReason()
        );
    }
}
