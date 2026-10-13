package com.todayyu.application.auth.dto.command;

import com.todayyu.domain.auth.vo.AccessToken;

public record SignoutCommand(
        AccessToken accessToken
) {
}
