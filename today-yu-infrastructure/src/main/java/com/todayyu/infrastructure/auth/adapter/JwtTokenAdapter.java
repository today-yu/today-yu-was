package com.todayyu.infrastructure.auth.adapter;

import com.todayyu.application.auth.port.TokenPort;
import com.todayyu.domain.auth.vo.AccessToken;
import com.todayyu.domain.auth.vo.RefreshToken;
import com.todayyu.domain.member.vo.MemberId;
import com.todayyu.domain.member.vo.Role;
import com.todayyu.infrastructure.common.annotation.Adapter;
import com.todayyu.infrastructure.jwt.properties.JwtProperties;
import com.todayyu.infrastructure.jwt.provider.JwtProvider;
import lombok.RequiredArgsConstructor;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

@Adapter
@RequiredArgsConstructor
public class JwtTokenAdapter implements TokenPort {
    private final JwtProvider jwtProvider;
    private final JwtProperties jwtProperties;
    
    private static final String ROLE_CLAIM_KEY = "role";
    private static final String TYPE_CLAIM_KEY = "type";
    private static final String TYPE_ACCESS_TOKEN = "access";
    private static final String TYPE_REFRESH_TOKEN = "refresh";
    
    /**
     * Access Token을 발급하는 메서드
     *
     * <p> {@link #issue(MemberId, Instant, Duration, Map)}를 호출하여 Access Token을 발급한다. </p>
     *
     * @param memberId 회원 ID
     * @param role 해당 회원의 역할({@link Role})
     * @param issuedAt 토큰 발급 시각
     * @return {@link AccessToken} 객체
     */
    @Override
    public AccessToken issueAccessToken(MemberId memberId, Role role, Instant issuedAt) {
        return new AccessToken(issue(
                memberId,
                issuedAt,
                jwtProperties.getAccessTokenExpiration(),
                Map.of(ROLE_CLAIM_KEY, role.name(), TYPE_CLAIM_KEY, TYPE_ACCESS_TOKEN)
        ));
    }
    
    /**
     * Refresh Token을 발급하는 메서드
     *
     * <p> {@link #issue(MemberId, Instant, Duration, Map)}를 호출하여 Refresh Token을 발급한다. </p>
     *
     * @param memberId 회원 ID
     * @param issuedAt 토큰 발급 시각
     * @return {@link RefreshToken} 객체
     */
    @Override
    public RefreshToken issueRefreshToken(MemberId memberId, Instant issuedAt) {
        return new RefreshToken(issue(
                memberId,
                issuedAt,
                jwtProperties.getRefreshTokenExpiration(),
                Map.of(TYPE_CLAIM_KEY, TYPE_REFRESH_TOKEN))
        );
    }
    
    /**
     * Token을 발급하는 메서드
     *
     * @param memberId 회원 ID
     * @param issuedAt 토큰 발급  시각
     * @param expiration 토큰 유효 기간
     * @param claims 클레임 목록
     * @return 토큰 값
     */
    private String issue(MemberId memberId, Instant issuedAt, Duration expiration, Map<String, String> claims) {
        return jwtProvider.issue(
                String.valueOf(memberId.getValue()),
                issuedAt,
                expiration,
                claims
        );
    }
}
