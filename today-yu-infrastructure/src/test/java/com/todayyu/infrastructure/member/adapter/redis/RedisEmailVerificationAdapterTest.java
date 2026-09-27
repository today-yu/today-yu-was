package com.todayyu.infrastructure.member.adapter.redis;

import com.todayyu.domain.member.entity.EmailVerification;
import com.todayyu.domain.member.vo.Email;
import com.todayyu.domain.member.vo.VerificationCode;
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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class RedisEmailVerificationAdapterTest {
    private static final Email EMAIL = new Email("user@example.com");
    private static final VerificationCode CODE = new VerificationCode("123456");
    private static final LocalDateTime ISSUED_AT = LocalDateTime.of(2026, 9, 27, 10, 0);

    @Mock
    private RedisEmailVerificationStore store;

    @InjectMocks
    private RedisEmailVerificationAdapter adapter;

    @Captor
    private ArgumentCaptor<RedisEmailVerificationValue> valueCaptor;

    @Test
    @DisplayName("이메일을 키로, 인증 코드와 발급 시각을 값으로 저장한다")
    void save() {
        EmailVerification verification = new EmailVerification(EMAIL, CODE, ISSUED_AT);

        adapter.save(verification);

        then(store).should().save(eq(EMAIL.getValue()), valueCaptor.capture(), eq(Duration.between(ISSUED_AT, verification.getExpiredAt())));
        RedisEmailVerificationValue saved = valueCaptor.getValue();
        assertEquals(CODE.getValue(), saved.getVerificationCode());
        assertEquals(ISSUED_AT, saved.getIssuedAt());
    }

    @Test
    @DisplayName("저장된 값을 이메일 인증으로 변환해 조회한다")
    void readByEmail() {
        RedisEmailVerificationValue value = new RedisEmailVerificationValue(CODE.getValue(), ISSUED_AT);
        given(store.get(EMAIL.getValue())).willReturn(Optional.of(value));

        EmailVerification found = adapter.readByEmail(EMAIL).orElseThrow();

        assertEquals(EMAIL, found.getEmail());
        assertEquals(CODE, found.getCode());
        assertEquals(ISSUED_AT, found.getIssuedAt());
    }

    @Test
    @DisplayName("저장된 이메일 인증이 없으면 빈 값을 반환한다")
    void readByEmailNotFound() {
        given(store.get(EMAIL.getValue())).willReturn(Optional.empty());

        assertTrue(adapter.readByEmail(EMAIL).isEmpty());
    }
}
