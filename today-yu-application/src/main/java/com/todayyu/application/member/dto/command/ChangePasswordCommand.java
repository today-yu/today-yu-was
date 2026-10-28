package com.todayyu.application.member.dto.command;

import com.todayyu.domain.member.vo.MemberId;
import com.todayyu.domain.member.vo.Password;

public record ChangePasswordCommand(
        MemberId memberId,
        Password currentPassword,
        Password newPassword
) {
}
