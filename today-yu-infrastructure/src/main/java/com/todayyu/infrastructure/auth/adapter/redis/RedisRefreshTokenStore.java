package com.todayyu.infrastructure.auth.adapter.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class RedisRefreshTokenStore {
    private final StringRedisTemplate redisTemplate;
    
    private static final String KEY_PREFIX = "refresh-token:";
    private static final long SCAN_COUNT = 100;
    
    public Optional<String> read(String memberId, String sessionId) {
        return Optional.ofNullable(redisTemplate.opsForValue().get(key(memberId, sessionId)));
    }
    
    public void save(String memberId, String sessionId, String refreshToken, Duration ttl) {
        redisTemplate.opsForValue().set(key(memberId, sessionId), refreshToken, ttl);
    }
    
    public void delete(String memberId, String sessionId) {
        redisTemplate.delete(key(memberId, sessionId));
    }
    
    public void deleteAll(String memberId) {
        ScanOptions options = ScanOptions.scanOptions()
                .match(KEY_PREFIX + memberId + ":*")
                .count(SCAN_COUNT)
                .build();
        
        List<String> keys = new ArrayList<>();
        
        try (Cursor<String> cursor = redisTemplate.scan(options)) {
            cursor.forEachRemaining(keys::add);
        }
        
        if (!keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }
    
    private String key(String memberId, String sessionId) {
        return KEY_PREFIX + memberId + ":" + sessionId;
    }
}
