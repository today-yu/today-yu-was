package com.todayyu.apis.user.member.dto;

import com.todayyu.application.member.dto.VerificationCodeIssueCommand;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

public record VerificationCodeIssueRequest(
        @NotNull(message = "이메일은 비어있을 수 없습니다.")
        @Email(message = "이메일 양식이 잘못되었습니다.")
        String email
) {
    public VerificationCodeIssueCommand toCommand() {
        return new VerificationCodeIssueCommand(email);
    }
}
