package com.todayyu.infrastructure.member.adapter.redis;

import com.todayyu.domain.common.vo.Email;
import com.todayyu.domain.member.entity.PasswordResetVerification;
import com.todayyu.domain.member.vo.VerificationCode;
import com.todayyu.domain.member.vo.VerificationFailCount;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class RedisPasswordResetVerificationAdapterTest {
    private static final Email EMAIL = new Email("user@example.com");
    private static final VerificationCode CODE = new VerificationCode("123456");
    private static final Duration EXPIRATION = Duration.ofMinutes(5);

    @Mock
    private RedisPasswordResetVerificationStore store;

    @InjectMocks
    private RedisPasswordResetVerificationAdapter adapter;

    @Captor
    private ArgumentCaptor<RedisPasswordResetVerificationValue> valueCaptor;

    @Captor
    private ArgumentCaptor<Duration> ttlCaptor;

    @Test
    @DisplayName("이메일을 키로, 인증 코드와 발급 시각, 실패 횟수를 값으로 저장한다")
    void save() {
        LocalDateTime issuedAt = LocalDateTime.now();
        PasswordResetVerification verification = PasswordResetVerification.of(
                EMAIL, CODE, issuedAt, VerificationFailCount.of(2));

        adapter.save(verification);

        then(store).should().save(eq(EMAIL.getValue()), valueCaptor.capture(), any(Duration.class));
        RedisPasswordResetVerificationValue saved = valueCaptor.getValue();
        assertEquals(EMAIL.getValue(), saved.getEmail());
        assertEquals(CODE.getValue(), saved.getCode());
        assertEquals(issuedAt, saved.getIssuedAt());
        assertEquals(2, saved.getFailCount());
    }

    @Test
    @DisplayName("새로 발급한 인증은 만료 시각까지 남은 시간(최대 5분) 동안 저장한다")
    void saveWithRemainingTtl() {
        PasswordResetVerification verification = PasswordResetVerification.issue(EMAIL, CODE, LocalDateTime.now());

        adapter.save(verification);

        then(store).should().save(eq(EMAIL.getValue()), any(), ttlCaptor.capture());
        Duration ttl = ttlCaptor.getValue();
        assertTrue(ttl.compareTo(EXPIRATION) <= 0);
        assertFalse(ttl.isZero() || ttl.isNegative());
    }

    @Test
    @DisplayName("발급 후 시간이 지난 인증을 다시 저장하면 TTL이 연장되지 않고 남은 시간만큼만 저장한다")
    void saveDoesNotExtendTtl() {
        PasswordResetVerification verification = PasswordResetVerification.issue(
                EMAIL, CODE, LocalDateTime.now().minusMinutes(3));

        adapter.save(verification);

        then(store).should().save(eq(EMAIL.getValue()), any(), ttlCaptor.capture());
        Duration ttl = ttlCaptor.getValue();
        assertTrue(ttl.compareTo(Duration.ofMinutes(2)) <= 0);
        assertFalse(ttl.isZero() || ttl.isNegative());
    }

    @Test
    @DisplayName("이미 만료된 인증은 저장하지 않는다")
    void saveExpiredVerification() {
        PasswordResetVerification verification = PasswordResetVerification.issue(
                EMAIL, CODE, LocalDateTime.now().minus(EXPIRATION).minusSeconds(1));

        adapter.save(verification);

        then(store).should(never()).save(anyString(), any(), any(Duration.class));
    }

    @Test
    @DisplayName("저장된 값을 비밀번호 재설정 인증으로 변환해 조회한다")
    void readByEmail() {
        LocalDateTime issuedAt = LocalDateTime.of(2026, 10, 22, 10, 0);
        RedisPasswordResetVerificationValue value = new RedisPasswordResetVerificationValue(
                EMAIL.getValue(), CODE.getValue(), issuedAt, 3);
        given(store.get(EMAIL.getValue())).willReturn(Optional.of(value));

        PasswordResetVerification found = adapter.readByEmail(EMAIL).orElseThrow();

        assertEquals(EMAIL, found.getEmail());
        assertEquals(CODE, found.getCode());
        assertEquals(issuedAt, found.getIssuedAt());
        assertEquals(VerificationFailCount.of(3), found.getFailCount());
    }

    @Test
    @DisplayName("저장된 비밀번호 재설정 인증이 없으면 빈 값을 반환한다")
    void readByEmailNotFound() {
        given(store.get(EMAIL.getValue())).willReturn(Optional.empty());

        assertTrue(adapter.readByEmail(EMAIL).isEmpty());
    }
}
