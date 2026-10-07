package com.todayyu.application.auth.dto;

import com.todayyu.domain.common.vo.Email;
import com.todayyu.domain.member.vo.Password;

public record SigninCommand (
        Email email,
        Password password
) {
}
