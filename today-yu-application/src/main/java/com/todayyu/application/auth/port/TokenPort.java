package com.todayyu.application.auth.port;

import com.todayyu.application.auth.dto.AccessTokenClaims;
import com.todayyu.application.auth.dto.RefreshTokenClaims;
import com.todayyu.domain.auth.vo.AccessToken;
import com.todayyu.domain.auth.vo.RefreshToken;
import com.todayyu.domain.member.vo.MemberId;
import com.todayyu.domain.member.vo.Role;

import java.time.Instant;

public interface TokenPort {
    AccessToken issueAccessToken(MemberId memberId, Role role, Instant issuedAt);
    RefreshToken issueRefreshToken(MemberId memberId, Instant issuedAt);
    AccessTokenClaims parseAccessToken(AccessToken accessToken);
    RefreshTokenClaims parseRefreshToken(RefreshToken refreshToken);
}
