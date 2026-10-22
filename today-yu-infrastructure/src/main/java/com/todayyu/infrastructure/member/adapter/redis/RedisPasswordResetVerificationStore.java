package com.todayyu.infrastructure.member.adapter.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.time.Duration;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class RedisPasswordResetVerificationStore {
    private final StringRedisTemplate redisTemplate;
    private final JsonMapper jsonMapper;
    
    private static final String KEY_PREFIX = "password-reset-verification:";
    
    public Optional<RedisPasswordResetVerificationValue> get(String email) {
        return Optional.ofNullable(redisTemplate.opsForValue().get(KEY_PREFIX + email))
                .map(json -> jsonMapper.readValue(json, RedisPasswordResetVerificationValue.class));
    }
    
    public void save(String email, RedisPasswordResetVerificationValue value, Duration ttl) {
        redisTemplate.opsForValue().set(KEY_PREFIX + email, jsonMapper.writeValueAsString(value), ttl);
    }
}
