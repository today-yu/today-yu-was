package com.todayyu.application.member.dto;

public record VerificationCodeVerifyCommand(
        String email,
        String code
) {
}
