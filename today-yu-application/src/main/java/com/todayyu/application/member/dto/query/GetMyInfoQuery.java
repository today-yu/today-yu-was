package com.todayyu.application.member.dto.query;

import com.todayyu.domain.member.vo.MemberId;

public record GetMyInfoQuery(
        MemberId memberId
) {
}
