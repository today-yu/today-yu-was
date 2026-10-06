package com.todayyu.application.auth.dto;

import com.todayyu.domain.auth.vo.AccessToken;

public record SignoutCommand(
        AccessToken accessToken
) {
}
