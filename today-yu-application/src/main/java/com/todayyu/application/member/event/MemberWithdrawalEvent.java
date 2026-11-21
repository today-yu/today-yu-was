package com.todayyu.application.member.event;

import com.todayyu.domain.common.event.EventId;
import com.todayyu.domain.member.vo.MemberId;
import com.todayyu.domain.member.vo.SocialIdentity;

import java.util.List;

public record MemberWithdrawalEvent(
        EventId eventId,
        MemberId memberId,
        List<SocialIdentity> socialIdentities
) {
    public MemberWithdrawalEvent(MemberId memberId, List<SocialIdentity> socialIdentities) {
        this(EventId.generate(), memberId, socialIdentities);
    }
}
