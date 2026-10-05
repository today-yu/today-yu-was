package com.todayyu.infrastructure.auth.adapter.redis;

import com.todayyu.application.auth.port.read.ReadBlacklistTokenPort;
import com.todayyu.application.auth.port.save.SaveBlacklistTokenPort;
import com.todayyu.domain.auth.vo.TokenId;
import com.todayyu.infrastructure.common.annotation.Adapter;
import lombok.RequiredArgsConstructor;

import java.time.Duration;
import java.time.Instant;

@Adapter
@RequiredArgsConstructor
public class RedisBlacklistTokenAdapter implements ReadBlacklistTokenPort, SaveBlacklistTokenPort {
    private final RedisBlacklistTokenStore blacklistTokenStore;
    
    /**
     * 특정 토큰 ID가 블랙리스트로 등록된 토큰 ID인지 여부를 확인하는 메서드
     *
     * @param tokenId 토큰 ID
     * @return 블랙리스트 여부
     */
    @Override
    public boolean isBlacklistToken(TokenId tokenId) {
        return blacklistTokenStore.exists(tokenId.getValue());
    }
    
    /**
     * 특정 토큰을 블랙리스트 토큰으로 저장하는 메서드
     *
     * <p> 토큰의 jti({@link TokenId})를 통해 블랙리스트 토큰으로 저장한다. </p>
     *
     * <p> 토큰의 남은 만료 기한만큼 TTL로 설정하여 저장한다. </p>
     *
     * @param tokenId 토큰 ID
     * @param expiresAt 토큰 만료 시각
     */
    @Override
    public void save(TokenId tokenId, Instant expiresAt) {
        String key = tokenId.getValue();
        Duration duration = Duration.between(Instant.now(), expiresAt);
        
        if (duration.isNegative() || duration.isZero()) {
            return;
        }
        
        blacklistTokenStore.save(key, duration);
    }
}
