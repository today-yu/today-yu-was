package com.todayyu.infrastructure.auth.adapter;

import com.todayyu.domain.auth.vo.AccessToken;
import com.todayyu.domain.auth.vo.RefreshToken;
import com.todayyu.domain.member.vo.MemberId;
import com.todayyu.domain.member.vo.Role;
import com.todayyu.infrastructure.jwt.properties.JwtProperties;
import com.todayyu.infrastructure.jwt.provider.JwtProvider;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class JwtTokenAdapterTest {
    private static final String KEY = "dG9kYXkteXUtand0LXRlc3Qtc2VjcmV0LWtleS0wMTIzNDU2Nzg5";
    private static final String ISSUER = "today-yu-test";
    private static final Duration ACCESS_TOKEN_EXPIRATION = Duration.ofMinutes(30);
    private static final Duration REFRESH_TOKEN_EXPIRATION = Duration.ofDays(14);
    private static final Instant ISSUED_AT = Instant.parse("2026-10-04T00:00:00Z");
    private static final MemberId MEMBER_ID = new MemberId(1L);

    private final JwtProperties properties =
            new JwtProperties(KEY, ISSUER, ACCESS_TOKEN_EXPIRATION, REFRESH_TOKEN_EXPIRATION);
    private final JwtTokenAdapter jwtTokenAdapter = new JwtTokenAdapter(new JwtProvider(properties), properties);

    @Test
    @DisplayName("Access Token은 Access Token 만료 시간으로 발급된다")
    void issueAccessTokenWithAccessTokenExpiration() {
        // when
        AccessToken accessToken = jwtTokenAdapter.issueAccessToken(MEMBER_ID, Role.MEMBER, ISSUED_AT);

        // then
        Claims claims = parse(accessToken.getValue());
        assertEquals(ISSUED_AT.plus(ACCESS_TOKEN_EXPIRATION), claims.getExpiration().toInstant());
    }

    @Test
    @DisplayName("Access Token에는 회원 아이디, 권한, Access Token 타입이 담긴다")
    void issueAccessTokenWithClaims() {
        // when
        AccessToken accessToken = jwtTokenAdapter.issueAccessToken(MEMBER_ID, Role.MEMBER, ISSUED_AT);

        // then
        Claims claims = parse(accessToken.getValue());
        assertEquals(ISSUER, claims.getIssuer());
        assertEquals("1", claims.getSubject());
        assertEquals(ISSUED_AT, claims.getIssuedAt().toInstant());
        assertEquals(Role.MEMBER.name(), claims.get("role", String.class));
        assertEquals("access", claims.get("type", String.class));
    }

    @Test
    @DisplayName("Refresh Token은 Refresh Token 만료 시간으로 발급된다")
    void issueRefreshTokenWithRefreshTokenExpiration() {
        // when
        RefreshToken refreshToken = jwtTokenAdapter.issueRefreshToken(MEMBER_ID, ISSUED_AT);

        // then
        Claims claims = parse(refreshToken.getValue());
        assertEquals(ISSUED_AT.plus(REFRESH_TOKEN_EXPIRATION), claims.getExpiration().toInstant());
    }

    @Test
    @DisplayName("Refresh Token에는 회원 아이디와 Refresh Token 타입만 담기고 권한은 담기지 않는다")
    void issueRefreshTokenWithClaims() {
        // when
        RefreshToken refreshToken = jwtTokenAdapter.issueRefreshToken(MEMBER_ID, ISSUED_AT);

        // then
        Claims claims = parse(refreshToken.getValue());
        assertEquals(ISSUER, claims.getIssuer());
        assertEquals("1", claims.getSubject());
        assertEquals("refresh", claims.get("type", String.class));
        assertNull(claims.get("role"));
    }

    @Test
    @DisplayName("같은 회원에게 같은 시각에 발급해도 토큰마다 다른 값이 생성된다")
    void issueUniqueTokenAtSameTime() {
        // when
        AccessToken first = jwtTokenAdapter.issueAccessToken(MEMBER_ID, Role.MEMBER, ISSUED_AT);
        AccessToken second = jwtTokenAdapter.issueAccessToken(MEMBER_ID, Role.MEMBER, ISSUED_AT);

        // then
        assertNotEquals(first.getValue(), second.getValue());
        assertNotEquals(parse(first.getValue()).getId(), parse(second.getValue()).getId());
    }

    private Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(properties.getSecretKey())
                .clock(() -> Date.from(ISSUED_AT))
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
