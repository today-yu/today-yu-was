package com.todayyu.infrastructure.auth.adapter.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class RedisRefreshTokenStore {
    private final StringRedisTemplate redisTemplate;
    
    private static final String KEY_PREFIX = "refresh-token:";
    
    public void save(String memberId, String sessionId, String refreshToken, Duration duration) {
        redisTemplate.opsForValue().set(key(memberId, sessionId), refreshToken, duration);
    }
    
    public void delete(String memberId, String sessionId) {
        redisTemplate.delete(key(memberId, sessionId));
    }
    
    private String key(String memberId, String sessionId) {
        return KEY_PREFIX + memberId + ":" + sessionId;
    }
}
