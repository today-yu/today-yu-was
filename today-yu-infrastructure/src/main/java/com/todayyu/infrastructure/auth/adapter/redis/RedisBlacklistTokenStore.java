package com.todayyu.infrastructure.auth.adapter.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class RedisBlacklistTokenStore {
    private final StringRedisTemplate redisTemplate;
    
    private static final String KEY_PREFIX = "blacklist-token:";
    private static final String BLACKLISTED = "1";
    
    public void save(String tokenId, Duration duration) {
        redisTemplate.opsForValue().set(KEY_PREFIX + tokenId, BLACKLISTED, duration);
    }
    
    public boolean exists(String tokenId) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(KEY_PREFIX + tokenId));
    }
}
