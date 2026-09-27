package com.todayyu.infrastructure.member.adapter.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.time.Duration;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class RedisEmailVerificationStore {
    private final StringRedisTemplate redisTemplate;
    private final JsonMapper jsonMapper;
    
    private static final String KEY_PREFIX = "email-verification:";
    
    public Optional<RedisEmailVerificationValue> get(String key) {
        return Optional.ofNullable(redisTemplate.opsForValue().get(KEY_PREFIX + key))
                .map(json -> jsonMapper.readValue(json, RedisEmailVerificationValue.class));
    }
    
    public void save(String key, RedisEmailVerificationValue value, Duration duration) {
        redisTemplate.opsForValue().set(KEY_PREFIX + key, jsonMapper.writeValueAsString(value), duration);
    }
}
