package com.todayyu.application.member.dto.command;

import com.todayyu.domain.common.vo.Email;

public record VerificationCodeIssueCommand(
        Email email
) {
}
