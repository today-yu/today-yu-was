package com.todayyu.application.info.dto.result;

import com.todayyu.domain.common.vo.Email;
import com.todayyu.domain.info.entity.Info;
import com.todayyu.domain.info.entity.InfoUpdateRequest;
import com.todayyu.domain.info.vo.InfoRequestStatus;
import com.todayyu.domain.info.vo.InfoUpdateRequestContent;
import com.todayyu.domain.info.vo.InfoUpdateRequestId;
import com.todayyu.domain.info.vo.RejectionReason;
import com.todayyu.domain.member.vo.MemberId;

import java.time.LocalDateTime;

public record InfoUpdateRequestDetailResult(
        InfoUpdateRequestId id,
        MemberId requesterId,
        InfoUpdateRequestContent content,
        Email contactEmail,
        LocalDateTime observedAt,
        LocalDateTime requestedAt,
        InfoRequestStatus status,
        LinkedInfoResult info,
        RejectionReason rejectionReason
) {
    public static InfoUpdateRequestDetailResult from(InfoUpdateRequest request, Info info) {
        LinkedInfoResult linkedInfoResult = (info == null) ? null : LinkedInfoResult.from(info);
        
        return new InfoUpdateRequestDetailResult(
                request.getId(),
                request.getRequesterId(),
                request.getContent(),
                request.getContactEmail(),
                request.getObservedAt(),
                request.getRequestedAt(),
                request.getStatus(),
                linkedInfoResult,
                request.getRejectionReason()
        );
    }
}
