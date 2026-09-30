package com.todayyu.application.member.dto;

import com.todayyu.domain.member.vo.Email;
import com.todayyu.domain.member.vo.VerificationCode;

public record VerificationCodeVerifyCommand(
        Email email,
        VerificationCode code
) {
}
