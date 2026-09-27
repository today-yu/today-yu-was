package com.todayyu.infrastructure.member.adapter.redis;

import com.todayyu.domain.member.entity.EmailVerification;
import com.todayyu.domain.member.vo.Email;
import com.todayyu.domain.member.vo.VerificationCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RedisEmailVerificationValueTest {
    private static final Email EMAIL = new Email("user@example.com");
    private static final LocalDateTime ISSUED_AT = LocalDateTime.of(2026, 9, 27, 10, 0);

    private final JsonMapper jsonMapper = JsonMapper.builder().build();

    @Test
    @DisplayName("JSON으로 직렬화한 값을 다시 역직렬화하면 같은 값이 된다")
    void jsonRoundTrip() {
        RedisEmailVerificationValue value = RedisEmailVerificationValue.from(
                new EmailVerification(EMAIL, new VerificationCode("123456"), ISSUED_AT));

        String json = jsonMapper.writeValueAsString(value);
        RedisEmailVerificationValue restored = jsonMapper.readValue(json, RedisEmailVerificationValue.class);

        assertEquals(value.getVerificationCode(), restored.getVerificationCode());
        assertEquals(value.getIssuedAt(), restored.getIssuedAt());
    }

    @Test
    @DisplayName("저장 값을 이메일 인증으로 변환하면 원래의 코드와 발급 시각을 가진다")
    void toEmailVerification() {
        EmailVerification verification = new EmailVerification(EMAIL, new VerificationCode("123456"), ISSUED_AT);

        EmailVerification restored = RedisEmailVerificationValue.from(verification).toEmailVerification(EMAIL);

        assertEquals(EMAIL, restored.getEmail());
        assertEquals(verification.getCode(), restored.getCode());
        assertEquals(ISSUED_AT, restored.getIssuedAt());
    }
}
