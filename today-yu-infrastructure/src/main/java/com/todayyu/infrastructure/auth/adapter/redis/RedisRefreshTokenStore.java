package com.todayyu.infrastructure.auth.adapter.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class RedisRefreshTokenStore {
    private final StringRedisTemplate redisTemplate;
    
    private static final String KEY_PREFIX = "refresh-token:";
    
    public Optional<String> read(String memberId, String sessionId) {
        return Optional.ofNullable(redisTemplate.opsForValue().get(key(memberId, sessionId)));
    }
    
    public void save(String memberId, String sessionId, String refreshToken, Duration ttl) {
        redisTemplate.opsForValue().set(key(memberId, sessionId), refreshToken, ttl);
    }
    
    public void delete(String memberId, String sessionId) {
        redisTemplate.delete(key(memberId, sessionId));
    }
    
    private String key(String memberId, String sessionId) {
        return KEY_PREFIX + memberId + ":" + sessionId;
    }
}
