package com.todayyu.infrastructure.info.adapter.redis;

import com.todayyu.domain.info.vo.InfoId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.RedisConnectionFailureException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;

@ExtendWith(MockitoExtension.class)
class RedisInfoViewCountAdapterTest {
    private static final InfoId INFO_ID = new InfoId(1L);
    private static final String INFO_ID_VALUE = "1";

    @Mock
    private RedisInfoViewCountStore store;

    @InjectMocks
    private RedisInfoViewCountAdapter adapter;

    @Test
    @DisplayName("정보 ID를 문자열로 변환하여 조회수 증가를 요청한다")
    void increase() {
        adapter.increase(INFO_ID);

        then(store).should().increase(INFO_ID_VALUE);
    }

    @Test
    @DisplayName("Redis 장애로 조회수 증가에 실패하면 예외를 던지지 않는다")
    void increaseWithRedisFailure() {
        willThrow(new RedisConnectionFailureException("Redis 연결 실패")).given(store).increase(INFO_ID_VALUE);

        assertDoesNotThrow(() -> adapter.increase(INFO_ID));
    }

    @Test
    @DisplayName("Redis 장애가 아닌 예외는 그대로 던진다")
    void increaseWithUnexpectedException() {
        willThrow(new IllegalStateException()).given(store).increase(INFO_ID_VALUE);

        assertThrows(IllegalStateException.class, () -> adapter.increase(INFO_ID));
    }
}
