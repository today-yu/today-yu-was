package com.todayyu.application.auth.dto;

import com.todayyu.domain.auth.vo.AccessToken;
import com.todayyu.domain.auth.vo.RefreshToken;

import java.time.Instant;

public record SigninResult (
        AccessToken accessToken,
        Instant accessTokenExpiresAt,
        RefreshToken refreshToken,
        Instant refreshTokenExpiresAt
) {
    public static SigninResult from(IssuedAccessToken issuedAccessToken, IssuedRefreshToken issuedRefreshToken) {
        return new SigninResult(
                issuedAccessToken.accessToken(),
                issuedAccessToken.expiresAt(),
                issuedRefreshToken.refreshToken(),
                issuedRefreshToken.expiresAt()
        );
    }
}
