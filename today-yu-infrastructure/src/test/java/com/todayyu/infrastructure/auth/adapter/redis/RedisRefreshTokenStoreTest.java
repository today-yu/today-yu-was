package com.todayyu.infrastructure.auth.adapter.redis;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.List;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

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

    @Test
    @DisplayName("refresh-token:{회원 ID}:* 패턴으로 조회한 해당 회원의 모든 Refresh Token을 삭제한다")
    void deleteAll() {
        // given
        List<String> keys = List.of("refresh-token:1:session-1", "refresh-token:1:session-2");
        Cursor<String> cursor = mockCursor(keys);
        ArgumentCaptor<ScanOptions> optionsCaptor = ArgumentCaptor.forClass(ScanOptions.class);
        given(redisTemplate.scan(optionsCaptor.capture())).willReturn(cursor);

        // when
        store.deleteAll(MEMBER_ID);

        // then
        assertEquals("refresh-token:1:*", optionsCaptor.getValue().getPattern());
        then(redisTemplate).should().delete(keys);
        then(cursor).should().close();
    }

    @Test
    @DisplayName("해당 회원의 Refresh Token이 없으면 삭제하지 않는다")
    void deleteAllWithNoKeys() {
        // given
        Cursor<String> cursor = mockCursor(List.of());
        given(redisTemplate.scan(any(ScanOptions.class))).willReturn(cursor);

        // when
        store.deleteAll(MEMBER_ID);

        // then
        then(redisTemplate).should(never()).delete(anyCollection());
        then(cursor).should().close();
    }

    @SuppressWarnings("unchecked")
    private Cursor<String> mockCursor(List<String> keys) {
        Cursor<String> cursor = mock(Cursor.class);
        willAnswer(invocation -> {
            Consumer<String> action = invocation.getArgument(0);
            keys.forEach(action);
            return null;
        }).given(cursor).forEachRemaining(any());
        return cursor;
    }
}
