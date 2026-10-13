package com.todayyu.application.info.dto.query;

import com.todayyu.domain.info.vo.InfoRegistrationRequestId;
import com.todayyu.domain.member.vo.MemberId;

public record GetInfoRegistrationRequestDetailQuery(
        InfoRegistrationRequestId id,
        MemberId memberId
) {
}
