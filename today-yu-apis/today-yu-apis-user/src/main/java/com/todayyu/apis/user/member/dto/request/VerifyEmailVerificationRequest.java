package com.todayyu.apis.user.member.dto.request;

import com.todayyu.application.member.dto.command.VerifyEmailVerificationCommand;
import com.todayyu.domain.common.vo.Email;
import com.todayyu.domain.member.vo.VerificationCode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record VerifyEmailVerificationRequest(
        @NotBlank(message = "이메일은 비어있을 수 없습니다.")
        @Pattern(regexp = Email.REGEX, flags = Pattern.Flag.CASE_INSENSITIVE, message = "이메일 양식이 잘못되었습니다.")
        String email,
        
        @NotBlank(message = "인증 코드는 비어있을 수 없습니다.")
        String code
) {
    public VerifyEmailVerificationCommand toCommand() {
        return new VerifyEmailVerificationCommand(new Email(email), new VerificationCode(code));
    }
}
