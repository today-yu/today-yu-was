package com.todayyu.application.auth.dto.result;

import com.todayyu.application.auth.dto.IssuedAccessToken;
import com.todayyu.application.auth.dto.IssuedRefreshToken;
import com.todayyu.domain.auth.vo.AccessToken;
import com.todayyu.domain.auth.vo.RefreshToken;

import java.time.Instant;

public record AuthTokenResult(
        AccessToken accessToken,
        Instant accessTokenExpiresAt,
        RefreshToken refreshToken,
        Instant refreshTokenExpiresAt
) {
    public static AuthTokenResult from(IssuedAccessToken issuedAccessToken, IssuedRefreshToken issuedRefreshToken) {
        return new AuthTokenResult(
                issuedAccessToken.accessToken(),
                issuedAccessToken.expiresAt(),
                issuedRefreshToken.refreshToken(),
                issuedRefreshToken.expiresAt()
        );
    }
}
