package com.todayyu.application.auth.port;

import com.todayyu.application.auth.port.dto.AccessTokenClaims;
import com.todayyu.application.auth.port.dto.IssuedAccessToken;
import com.todayyu.application.auth.port.dto.IssuedRefreshToken;
import com.todayyu.application.auth.port.dto.RefreshTokenClaims;
import com.todayyu.domain.auth.vo.AccessToken;
import com.todayyu.domain.auth.vo.RefreshToken;
import com.todayyu.domain.auth.vo.SessionId;
import com.todayyu.domain.member.vo.MemberId;
import com.todayyu.domain.member.vo.Role;

import java.time.Instant;

public interface TokenPort {
    IssuedAccessToken issueAccessToken(MemberId memberId, Role role, SessionId sessionId, Instant issuedAt);
    IssuedRefreshToken issueRefreshToken(MemberId memberId, SessionId sessionId, Instant issuedAt);
    AccessTokenClaims parseAccessToken(AccessToken accessToken);
    RefreshTokenClaims parseRefreshToken(RefreshToken refreshToken);
}
