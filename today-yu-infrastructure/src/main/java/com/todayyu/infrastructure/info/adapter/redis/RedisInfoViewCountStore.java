package com.todayyu.infrastructure.info.adapter.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedisInfoViewCountStore {
    private final StringRedisTemplate redisTemplate;
    
    private static final String KEY = "info-view-count";
    
    public void increase(String infoId) {
        redisTemplate.opsForHash().increment(KEY, infoId, 1);
    }
}
