package com.todayyu.application.auth.dto.command;

import com.todayyu.domain.member.vo.AuthProvider;

public record SocialSigninCommand(
        AuthProvider provider,
        String authorizationCode
) {
}
