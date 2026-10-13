package com.todayyu.application.auth.port.dto;

import com.todayyu.domain.auth.vo.AccessToken;

import java.time.Instant;

public record IssuedAccessToken(
        AccessToken accessToken,
        Instant expiresAt
) {
}
