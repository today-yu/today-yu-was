package com.todayyu.infrastructure.jwt.provider;

import com.todayyu.infrastructure.jwt.properties.JwtProperties;
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
}
