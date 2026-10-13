package com.todayyu.apis.user.auth.dto.response;

import com.todayyu.apis.common.auth.AuthScheme;
import com.todayyu.application.auth.dto.result.AuthTokenResult;

import java.time.Instant;

public record AuthTokenResponse(
        String tokenType,
        String accessToken,
        Instant accessTokenExpiresAt,
        String refreshToken,
        Instant refreshTokenExpiresAt
) {
    public static AuthTokenResponse from(AuthTokenResult result) {
        return new AuthTokenResponse(
                AuthScheme.BEARER,
                result.accessToken().getValue(),
                result.accessTokenExpiresAt(),
                result.refreshToken().getValue(),
                result.refreshTokenExpiresAt()
        );
    }
}
