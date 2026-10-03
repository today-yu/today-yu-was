package com.todayyu.infrastructure.auth.adapter;

import com.todayyu.application.auth.dto.AccessTokenClaims;
import com.todayyu.application.auth.dto.RefreshTokenClaims;
import com.todayyu.application.auth.error.BusinessAuthErrorType;
import com.todayyu.common.exception.CustomException;
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
import java.time.temporal.ChronoUnit;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JwtTokenAdapterTest {
    private static final String KEY = "dG9kYXkteXUtand0LXRlc3Qtc2VjcmV0LWtleS0wMTIzNDU2Nzg5";
    private static final String OTHER_KEY = "b3RoZXIta2V5LWZvci10b2RheS15dS1qd3QtdGVzdC0wMTIzNDU2";
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

    @Test
    @DisplayName("발급한 Access Token을 파싱하면 회원 아이디, 권한, 만료 시각을 얻는다")
    void parseAccessToken() {
        // given
        Instant issuedAt = now();
        AccessToken accessToken = jwtTokenAdapter.issueAccessToken(MEMBER_ID, Role.MEMBER, issuedAt);

        // when
        AccessTokenClaims claims = jwtTokenAdapter.parseAccessToken(accessToken);

        // then
        assertEquals(MEMBER_ID, claims.memberId());
        assertEquals(Role.MEMBER, claims.role());
        assertEquals(issuedAt.plus(ACCESS_TOKEN_EXPIRATION), claims.expiresAt());
    }

    @Test
    @DisplayName("발급한 Refresh Token을 파싱하면 회원 아이디와 만료 시각을 얻는다")
    void parseRefreshToken() {
        // given
        Instant issuedAt = now();
        RefreshToken refreshToken = jwtTokenAdapter.issueRefreshToken(MEMBER_ID, issuedAt);

        // when
        RefreshTokenClaims claims = jwtTokenAdapter.parseRefreshToken(refreshToken);

        // then
        assertEquals(MEMBER_ID, claims.memberId());
        assertEquals(issuedAt.plus(REFRESH_TOKEN_EXPIRATION), claims.expiresAt());
    }

    @Test
    @DisplayName("Refresh Token을 Access Token으로 파싱하면 유효하지 않은 토큰 예외가 발생한다")
    void parseRefreshTokenAsAccessToken() {
        // given
        RefreshToken refreshToken = jwtTokenAdapter.issueRefreshToken(MEMBER_ID, now());
        AccessToken disguised = new AccessToken(refreshToken.getValue());

        // when
        CustomException exception = assertThrows(CustomException.class,
                () -> jwtTokenAdapter.parseAccessToken(disguised));

        // then
        assertEquals(BusinessAuthErrorType.INVALID_TOKEN, exception.getErrorType());
    }

    @Test
    @DisplayName("Access Token을 Refresh Token으로 파싱하면 유효하지 않은 토큰 예외가 발생한다")
    void parseAccessTokenAsRefreshToken() {
        // given
        AccessToken accessToken = jwtTokenAdapter.issueAccessToken(MEMBER_ID, Role.MEMBER, now());
        RefreshToken disguised = new RefreshToken(accessToken.getValue());

        // when
        CustomException exception = assertThrows(CustomException.class,
                () -> jwtTokenAdapter.parseRefreshToken(disguised));

        // then
        assertEquals(BusinessAuthErrorType.INVALID_TOKEN, exception.getErrorType());
    }

    @Test
    @DisplayName("만료된 Access Token을 파싱하면 만료된 토큰 예외가 발생한다")
    void parseExpiredAccessToken() {
        // given
        Instant issuedAt = now().minus(ACCESS_TOKEN_EXPIRATION).minus(Duration.ofMinutes(1));
        AccessToken accessToken = jwtTokenAdapter.issueAccessToken(MEMBER_ID, Role.MEMBER, issuedAt);

        // when
        CustomException exception = assertThrows(CustomException.class,
                () -> jwtTokenAdapter.parseAccessToken(accessToken));

        // then
        assertEquals(BusinessAuthErrorType.EXPIRED_TOKEN, exception.getErrorType());
    }

    @Test
    @DisplayName("만료된 Refresh Token을 파싱하면 만료된 토큰 예외가 발생한다")
    void parseExpiredRefreshToken() {
        // given
        Instant issuedAt = now().minus(REFRESH_TOKEN_EXPIRATION).minus(Duration.ofMinutes(1));
        RefreshToken refreshToken = jwtTokenAdapter.issueRefreshToken(MEMBER_ID, issuedAt);

        // when
        CustomException exception = assertThrows(CustomException.class,
                () -> jwtTokenAdapter.parseRefreshToken(refreshToken));

        // then
        assertEquals(BusinessAuthErrorType.EXPIRED_TOKEN, exception.getErrorType());
    }

    @Test
    @DisplayName("다른 키로 서명된 토큰을 파싱하면 유효하지 않은 토큰 예외가 발생한다")
    void parseTokenSignedWithOtherKey() {
        // given
        JwtProperties otherProperties =
                new JwtProperties(OTHER_KEY, ISSUER, ACCESS_TOKEN_EXPIRATION, REFRESH_TOKEN_EXPIRATION);
        JwtTokenAdapter otherAdapter = new JwtTokenAdapter(new JwtProvider(otherProperties), otherProperties);
        AccessToken accessToken = otherAdapter.issueAccessToken(MEMBER_ID, Role.MEMBER, now());

        // when
        CustomException exception = assertThrows(CustomException.class,
                () -> jwtTokenAdapter.parseAccessToken(accessToken));

        // then
        assertEquals(BusinessAuthErrorType.INVALID_TOKEN, exception.getErrorType());
    }

    @Test
    @DisplayName("JWT 형식이 아닌 토큰을 파싱하면 유효하지 않은 토큰 예외가 발생한다")
    void parseMalformedToken() {
        // given
        AccessToken accessToken = new AccessToken("not.a.jwt");

        // when
        CustomException exception = assertThrows(CustomException.class,
                () -> jwtTokenAdapter.parseAccessToken(accessToken));

        // then
        assertEquals(BusinessAuthErrorType.INVALID_TOKEN, exception.getErrorType());
    }

    /**
     * JWT의 시각 claim은 초 단위이므로, 비교를 위해 현재 시각을 초 단위로 절삭한다.
     */
    private Instant now() {
        return Instant.now().truncatedTo(ChronoUnit.SECONDS);
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
