package com.todayyu.application.auth.dto;

import com.todayyu.domain.auth.vo.RefreshToken;

import java.time.Instant;

public record IssuedRefreshToken(
        RefreshToken refreshToken,
        Instant expiresAt
) { }
