package com.todayyu.application.info.dto.result;

import com.todayyu.application.info.port.dto.InfoRegistrationRequestView;
import com.todayyu.application.info.port.dto.InfoUpdateRequestView;
import com.todayyu.domain.member.vo.MemberId;
import com.todayyu.domain.member.vo.Nickname;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record ContributionResult(
        MemberId memberId,
        Nickname nickname,
        ContributionType type,
        Long requestId,
        LocalDateTime requestedAt
) {
    public static ContributionResult from(InfoRegistrationRequestView view) {
        return ContributionResult.builder()
                .memberId(view.request().getRequesterId())
                .nickname(view.requesterNickname())
                .type(ContributionType.REGISTRATION)
                .requestId(view.request().getId().getValue())
                .requestedAt(view.request().getRequestedAt())
                .build();
    }
    
    public static ContributionResult from(InfoUpdateRequestView view) {
        return ContributionResult.builder()
                .memberId(view.request().getRequesterId())
                .nickname(view.requesterNickname())
                .type(ContributionType.UPDATE)
                .requestId(view.request().getId().getValue())
                .requestedAt(view.request().getRequestedAt())
                .build();
    }
}
