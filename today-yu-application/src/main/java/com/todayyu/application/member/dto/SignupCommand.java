package com.todayyu.application.member.dto;

import com.todayyu.domain.member.vo.Email;
import com.todayyu.domain.member.vo.Password;

public record SignupCommand(
        Email email,
        Password password
) {
}
