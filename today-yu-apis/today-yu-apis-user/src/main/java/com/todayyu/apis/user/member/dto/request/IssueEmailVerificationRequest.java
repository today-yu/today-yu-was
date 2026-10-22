package com.todayyu.apis.user.member.dto.request;

import com.todayyu.application.member.dto.command.IssueEmailVerificationCommand;
import com.todayyu.domain.common.vo.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record IssueEmailVerificationRequest(
        @NotNull(message = "이메일은 비어있을 수 없습니다.")
        @Pattern(regexp = Email.REGEX, flags = Pattern.Flag.CASE_INSENSITIVE, message = "이메일 양식이 잘못되었습니다.")
        String email
) {
    public IssueEmailVerificationCommand toCommand() {
        return new IssueEmailVerificationCommand(new Email(email));
    }
}
