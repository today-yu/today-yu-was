package com.todayyu.apis.user.member.dto;

import com.todayyu.application.member.dto.VerificationCodeVerifyCommand;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record VerificationCodeVerifyRequest(
        @NotBlank(message = "이메일은 비어있을 수 없습니다.")
        @Email(message = "이메일 양식이 잘못되었습니다.")
        String email,
        
        @NotBlank(message = "인증 코드는 비어있을 수 없습니다.")
        String code
) {
    public VerificationCodeVerifyCommand toCommand() {
        return new VerificationCodeVerifyCommand(email, code);
    }
}
