package com.todayyu.infrastructure.auth.adapter.jwt;

import com.todayyu.application.auth.dto.AccessTokenClaims;
import com.todayyu.application.auth.dto.IssuedAccessToken;
import com.todayyu.application.auth.dto.IssuedRefreshToken;
import com.todayyu.application.auth.dto.RefreshTokenClaims;
import com.todayyu.application.auth.error.BusinessAuthErrorType;
import com.todayyu.application.auth.port.TokenPort;
import com.todayyu.common.exception.CustomException;
import com.todayyu.domain.auth.vo.AccessToken;
import com.todayyu.domain.auth.vo.RefreshToken;
import com.todayyu.domain.member.vo.MemberId;
import com.todayyu.domain.member.vo.Role;
import com.todayyu.infrastructure.common.annotation.Adapter;
import com.todayyu.infrastructure.jwt.properties.JwtProperties;
import com.todayyu.infrastructure.jwt.provider.JwtProvider;
import io.jsonwebtoken.Claims;
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
     * <p> 토큰 만료 기한을 같이 포함한 DTO를 반환한다. </p>
     *
     * @param memberId 회원 ID
     * @param role 해당 회원의 역할({@link Role})
     * @param issuedAt 토큰 발급 시각
     * @return {@link IssuedAccessToken} 객체
     */
    @Override
    public IssuedAccessToken issueAccessToken(MemberId memberId, Role role, Instant issuedAt) {
        String token = issue(
                memberId,
                issuedAt,
                jwtProperties.getAccessTokenExpiration(),
                Map.of(ROLE_CLAIM_KEY, role.name(), TYPE_CLAIM_KEY, TYPE_ACCESS_TOKEN)
        );
        Instant expiresAt = issuedAt.plus(jwtProperties.getAccessTokenExpiration());
        
        return new IssuedAccessToken(new AccessToken(token), expiresAt);
    }
    
    /**
     * Refresh Token을 발급하는 메서드
     *
     * <p> {@link #issue(MemberId, Instant, Duration, Map)}를 호출하여 Refresh Token을 발급한다. </p>
     * <p> 토큰 만료 기한을 같이 포함한 DTO를 반환한다. </p>
     *
     * @param memberId 회원 ID
     * @param issuedAt 토큰 발급 시각
     * @return {@link IssuedRefreshToken} 객체
     */
    @Override
    public IssuedRefreshToken issueRefreshToken(MemberId memberId, Instant issuedAt) {
        String token = issue(
                memberId,
                issuedAt,
                jwtProperties.getRefreshTokenExpiration(),
                Map.of(TYPE_CLAIM_KEY, TYPE_REFRESH_TOKEN)
        );
        Instant expiresAt = issuedAt.plus(jwtProperties.getRefreshTokenExpiration());
        
        return new IssuedRefreshToken(new RefreshToken(token), expiresAt);
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
    
    /**
     * Access Token을 파싱하는 메서드
     *
     * <p> {@link #validateType(Claims, String)}을 호출하여, Access Token 타입이 아닌 경우 예외를 발생한다. </p>
     *
     * @param accessToken Access Token
     * @return Access Token의 클레임
     */
    @Override
    public AccessTokenClaims parseAccessToken(AccessToken accessToken) {
        Claims claims = jwtProvider.parse(accessToken.getValue());
        validateType(claims, TYPE_ACCESS_TOKEN);
        
        return new AccessTokenClaims(parseMemberId(claims), parseRole(claims), parseExpiration(claims));
    }
    
    /**
     * Refresh Token을 파싱하는 메서드
     *
     * <p> {@link #validateType(Claims, String)}을 호출하여, Refresh Token 타입이 아닌 경우 예외를 발생한다. </p>
     *
     * @param refreshToken Refresh Token
     * @return Refresh Token 클레임
     */
    @Override
    public RefreshTokenClaims parseRefreshToken(RefreshToken refreshToken) {
        Claims claims = jwtProvider.parse(refreshToken.getValue());
        validateType(claims, TYPE_REFRESH_TOKEN);
        
        return new RefreshTokenClaims(parseMemberId(claims), parseExpiration(claims));
    }
    
    /**
     * 토큰 타입을 검증하는 메서드
     *
     * @param claims 토큰 클레임 {@link Claims} 객체
     * @param requiredType 요구되는 토큰 타입
     * @throws CustomException 토큰 타입 검증 중 예외가 발생한 경우
     * <ul>
     *     <li> {@link BusinessAuthErrorType#INVALID_TOKEN} - 토큰의 타입 정보가 null인 경우 </li>
     *     <li> {@link BusinessAuthErrorType#INVALID_TOKEN} - 토큰의 타입 정보가 불일치하는 경우 </li>
     * </ul>
     */
    private void validateType(Claims claims, String requiredType) {
        String type = claims.get(TYPE_CLAIM_KEY, String.class);
        
        if (type == null) {
            throw new CustomException(BusinessAuthErrorType.INVALID_TOKEN, "토큰에 타입(type) 정보가 없습니다.");
        }
        
        if (!requiredType.equals(type)) {
            throw new CustomException(BusinessAuthErrorType.INVALID_TOKEN, "토큰 타입이 잘못되었습니다.");
        }
    }
    
    /**
     * 토큰을 파싱하여 회원 ID를 반환하는 메서드
     *
     * @param claims 토큰 클레임 ({@link Claims}) 객체
     * @return 회원 ID 객체
     * @throws CustomException {@link BusinessAuthErrorType#INVALID_TOKEN} - 토큰 Subject가 잘못된 경우
     */
    private MemberId parseMemberId(Claims claims) {
        try {
            return new MemberId(Long.parseLong(claims.getSubject()));
        } catch (IllegalArgumentException e) {
            throw new CustomException(BusinessAuthErrorType.INVALID_TOKEN, "토큰 Subject가 잘못되었습니다.", e);
        }
    }
    
    /**
     * 토큰을 파싱하여 만료 기한을 반환하는 메서드
     *
     * @param claims 토큰 클레임 ({@link Claims}) 객체
     * @return 토큰 만료 기한
     */
    private Instant parseExpiration(Claims claims) {
        return claims.getExpiration().toInstant();
    }
    
    /**
     * 토큰을 파싱하여 회원 역할({@link Role})을 반환하는 메서드
     *
     * @param claims 토큰 클레임 ({@link Claims}) 객체
     * @return 회원 역할
     * @throws CustomException 회원 역할 파싱 중 예외가 발생한 경우
     * <ul>
     *     <li> {@link BusinessAuthErrorType#INVALID_TOKEN} - 토큰 내 역할(Role) 클레임이 존재하지 않는 경우 </li>
     *     <li> {@link BusinessAuthErrorType#INVALID_TOKEN} - 토큰 내 역할(Role) 정보를 {@link Role} enum으로 변환 불가능한 경우 </li>
     * </ul>
     */
    private Role parseRole(Claims claims) {
        String role = claims.get(ROLE_CLAIM_KEY, String.class);
        
        if (role == null) {
            throw new CustomException(BusinessAuthErrorType.INVALID_TOKEN, "토큰에 역할(Role) 정보가 없습니다.");
        }
        
        try {
            return Role.valueOf(role);
        } catch (IllegalArgumentException e) {
            throw new CustomException(BusinessAuthErrorType.INVALID_TOKEN, "토큰에 역할(Role) 정보가 잘못되었습니다.", e);
        }
    }
}
