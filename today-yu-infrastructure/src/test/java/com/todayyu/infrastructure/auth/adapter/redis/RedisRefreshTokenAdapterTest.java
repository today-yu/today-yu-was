package com.todayyu.infrastructure.auth.adapter.redis;

import com.todayyu.application.auth.dto.IssuedRefreshToken;
import com.todayyu.domain.auth.vo.RefreshToken;
import com.todayyu.domain.member.vo.MemberId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class RedisRefreshTokenAdapterTest {
    private static final MemberId MEMBER_ID = new MemberId(1L);
    private static final RefreshToken REFRESH_TOKEN = new RefreshToken("refresh-token");
    private static final Duration REFRESH_TOKEN_EXPIRATION = Duration.ofDays(14);

    @Mock
    private RedisRefreshTokenStore store;

    @InjectMocks
    private RedisRefreshTokenAdapter adapter;

    @Captor
    private ArgumentCaptor<Duration> durationCaptor;

    @Test
    @DisplayName("회원 ID를 키로, Refresh Token을 값으로 만료 시각까지 남은 시간 동안 저장한다")
    void save() {
        IssuedRefreshToken issuedRefreshToken = new IssuedRefreshToken(REFRESH_TOKEN, Instant.now().plus(REFRESH_TOKEN_EXPIRATION));

        adapter.save(MEMBER_ID, issuedRefreshToken);

        then(store).should().save(eq(String.valueOf(MEMBER_ID.getValue())), eq(REFRESH_TOKEN.getValue()), durationCaptor.capture());
        Duration duration = durationCaptor.getValue();
        assertTrue(duration.compareTo(REFRESH_TOKEN_EXPIRATION) <= 0);
        assertFalse(duration.isZero() || duration.isNegative());
    }

    @Test
    @DisplayName("이미 만료된 Refresh Token은 저장하지 않고 예외를 던진다")
    void saveExpiredToken() {
        IssuedRefreshToken issuedRefreshToken = new IssuedRefreshToken(REFRESH_TOKEN, Instant.now().minusSeconds(1));

        assertThrows(IllegalArgumentException.class, () -> adapter.save(MEMBER_ID, issuedRefreshToken));
        then(store).should(never()).save(anyString(), anyString(), any(Duration.class));
    }
}
