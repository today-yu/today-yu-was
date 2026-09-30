package com.todayyu.application.member.dto;

import com.todayyu.domain.member.vo.Email;

public record VerificationCodeIssueCommand(
        Email email
) {
}
