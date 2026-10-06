package com.todayyu.apis.user.auth.dto;

import com.todayyu.apis.common.auth.AuthScheme;
import com.todayyu.application.auth.dto.AuthTokenResult;

import java.time.Instant;

public record SigninResponse(
        String tokenType,
        String accessToken,
        Instant accessTokenExpiresAt,
        String refreshToken,
        Instant refreshTokenExpiresAt
) {
    public static SigninResponse from(AuthTokenResult result) {
        return new SigninResponse(
                AuthScheme.BEARER,
                result.accessToken().getValue(),
                result.accessTokenExpiresAt(),
                result.refreshToken().getValue(),
                result.refreshTokenExpiresAt()
        );
    }
}
