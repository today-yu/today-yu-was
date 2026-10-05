package com.todayyu.infrastructure.auth.adapter.redis;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class RedisRefreshTokenStoreTest {
    private static final String MEMBER_ID = "1";
    private static final String SESSION_ID = "session-id";
    private static final String REFRESH_TOKEN = "refresh-token";
    private static final Duration DURATION = Duration.ofDays(14);
    private static final String EXPECTED_KEY = "refresh-token:1:session-id";

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private RedisRefreshTokenStore store;

    @Test
    @DisplayName("Refresh Token을 refresh-token:{회원 ID}:{세션 ID} 키로 저장한다")
    void save() {
        given(redisTemplate.opsForValue()).willReturn(valueOperations);

        store.save(MEMBER_ID, SESSION_ID, REFRESH_TOKEN, DURATION);

        then(valueOperations).should().set(EXPECTED_KEY, REFRESH_TOKEN, DURATION);
    }

    @Test
    @DisplayName("refresh-token:{회원 ID}:{세션 ID} 키로 저장된 Refresh Token을 삭제한다")
    void delete() {
        store.delete(MEMBER_ID, SESSION_ID);

        then(redisTemplate).should().delete(EXPECTED_KEY);
    }
}
