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
    
    public void save(String key, String refreshToken, Duration duration) {
        redisTemplate.opsForValue().set(KEY_PREFIX + key, refreshToken, duration);
    }
}
