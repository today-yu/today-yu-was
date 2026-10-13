package com.todayyu.application.auth.dto.result;

import com.todayyu.domain.member.vo.MemberId;
import com.todayyu.domain.member.vo.Role;

public record AuthenticatedMemberResult(
        MemberId memberId,
        Role role
) {
}
