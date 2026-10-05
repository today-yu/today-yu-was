package com.todayyu.application.auth.dto;

import com.todayyu.domain.member.vo.MemberId;
import com.todayyu.domain.member.vo.Role;

public record AuthenticatedMember(
        MemberId memberId,
        Role role
) {
}
