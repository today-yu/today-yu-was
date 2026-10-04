package com.todayyu.infrastructure.auth.adapter.redis;

import com.todayyu.application.auth.dto.IssuedRefreshToken;
import com.todayyu.application.auth.port.save.SaveRefreshTokenPort;
import com.todayyu.domain.member.vo.MemberId;
import com.todayyu.infrastructure.common.annotation.Adapter;
import lombok.RequiredArgsConstructor;

import java.time.Duration;
import java.time.Instant;

@Adapter
@RequiredArgsConstructor
public class RedisRefreshTokenAdapter implements SaveRefreshTokenPort {
    private final RedisRefreshTokenStore refreshTokenStore;
    
    /**
     * Redis에 Refresh Token을 저장하는 메서드
     *
     * @param memberId 회원 ID
     * @param issuedRefreshToken {@link IssuedRefreshToken} 객체
     * @throws IllegalArgumentException 토큰이 만료된 경우
     */
    @Override
    public void save(MemberId memberId, IssuedRefreshToken issuedRefreshToken) {
        String key = String.valueOf(memberId.getValue());
        String token = issuedRefreshToken.refreshToken().getValue();
        Duration duration = Duration.between(Instant.now(), issuedRefreshToken.expiresAt());
        
        if (duration.isZero() || duration.isNegative()) {
            throw new IllegalArgumentException("이미 만료된 Refresh Token은 저장할 수 없습니다.");
        }
        
        refreshTokenStore.save(key, token, duration);
    }
}
