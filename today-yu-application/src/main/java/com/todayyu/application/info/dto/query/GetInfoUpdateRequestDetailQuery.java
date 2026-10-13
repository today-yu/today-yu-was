package com.todayyu.application.info.dto.query;

import com.todayyu.domain.info.vo.InfoUpdateRequestId;
import com.todayyu.domain.member.vo.MemberId;

public record GetInfoUpdateRequestDetailQuery(
        InfoUpdateRequestId id,
        MemberId memberId
) {
}
