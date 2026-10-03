package com.todayyu.infrastructure.jwt.provider;

import com.todayyu.application.auth.error.BusinessAuthErrorType;
import com.todayyu.common.exception.CustomException;
import com.todayyu.infrastructure.jwt.properties.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JwtProvider {
    private final JwtProperties properties;
    
    /**
     * JWT 토큰을 발급하는 메서드
     *
     * @param subject 토큰 Subject (회원 ID)
     * @param issuedAt 토큰 발급 시각
     * @param duration 토큰 유효 기간
     * @param claims 클레임 목록
     * @return 토큰 문자열
     */
    public String issue(String subject, Instant issuedAt, Duration duration, Map<String, String> claims) {
        return Jwts.builder()
                .issuer(properties.getIssuer())
                .subject(subject)
                .id(UUID.randomUUID().toString())
                .issuedAt(Date.from(issuedAt))
                .expiration(Date.from(issuedAt.plus(duration)))
                .signWith(properties.getSecretKey())
                .claims(claims)
                .compact();
    }
    
    /**
     * JWT 토큰을 파싱하는 메서드
     *
     * @param token 토큰
     * @return 파싱 결과 {@link Claims} 객체
     * @throws CustomException 토큰 파싱 중 예외가 발생한 경우
     * <ul>
     *     <li> {@link BusinessAuthErrorType#EXPIRED_TOKEN} - 토큰이 만료된 경우 </li>
     *     <li> {@link BusinessAuthErrorType#INVALID_TOKEN} - 토큰 파싱에 실패한 경우 </li>
     * </ul>
     */
    public Claims parse(String token) {
        try {
            return Jwts.parser()
                .verifyWith(properties.getSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        } catch (ExpiredJwtException e) {
            throw new CustomException(BusinessAuthErrorType.EXPIRED_TOKEN, "만료된 토큰입니다.");
        } catch (JwtException | IllegalArgumentException e) {
            throw new CustomException(BusinessAuthErrorType.INVALID_TOKEN, "유효하지 않은 토큰입니다.", e);
        }
    }
}
