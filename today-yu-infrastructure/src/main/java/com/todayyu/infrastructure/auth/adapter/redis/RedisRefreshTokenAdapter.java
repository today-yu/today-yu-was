package com.todayyu.infrastructure.auth.adapter.redis;

import com.todayyu.application.auth.dto.IssuedRefreshToken;
import com.todayyu.application.auth.port.read.ReadRefreshTokenPort;
import com.todayyu.application.auth.port.save.SaveRefreshTokenPort;
import com.todayyu.domain.auth.vo.RefreshToken;
import com.todayyu.domain.auth.vo.SessionId;
import com.todayyu.domain.member.vo.MemberId;
import com.todayyu.infrastructure.common.annotation.Adapter;
import lombok.RequiredArgsConstructor;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

@Adapter
@RequiredArgsConstructor
public class RedisRefreshTokenAdapter implements ReadRefreshTokenPort, SaveRefreshTokenPort {
    private final RedisRefreshTokenStore refreshTokenStore;
    
    /**
     * Redis에서 Refresh Token을 조회하는 메서드
     *
     * @param memberId 회원 ID
     * @param sessionId 세션 ID
     * @return
     */
    @Override
    public Optional<RefreshToken> read(MemberId memberId, SessionId sessionId) {
        return refreshTokenStore.read(String.valueOf(memberId.getValue()), sessionId.getValue())
                .map(refreshToken -> new RefreshToken(refreshToken));
    }
    
    /**
     * Redis에 Refresh Token을 저장하는 메서드
     *
     * @param memberId 회원 ID
     * @param sessionId 세션 ID
     * @param issuedRefreshToken {@link IssuedRefreshToken} 객체
     * @throws IllegalArgumentException 토큰이 만료된 경우
     */
    @Override
    public void save(MemberId memberId, SessionId sessionId, IssuedRefreshToken issuedRefreshToken) {
        String memberIdValue = String.valueOf(memberId.getValue());
        String sessionIdValue = sessionId.getValue();
        String token = issuedRefreshToken.refreshToken().getValue();
        Duration duration = Duration.between(Instant.now(), issuedRefreshToken.expiresAt());
        
        if (duration.isZero() || duration.isNegative()) {
            throw new IllegalArgumentException("이미 만료된 Refresh Token은 저장할 수 없습니다.");
        }
        
        refreshTokenStore.save(memberIdValue, sessionIdValue, token, duration);
    }
    
    /**
     * Redis에 저장된 Refresh Token을 제거하는 메서드
     *
     * @param memberId 회원 ID
     * @param sessionId 세션 ID
     */
    @Override
    public void delete(MemberId memberId, SessionId sessionId) {
        refreshTokenStore.delete(String.valueOf(memberId.getValue()), sessionId.getValue());
    }
}
