package com.todayyu.application.member.dto.command;

import com.todayyu.domain.common.vo.Email;
import com.todayyu.domain.member.vo.VerificationCode;

public record VerificationCodeVerifyCommand(
        Email email,
        VerificationCode code
) {
}
