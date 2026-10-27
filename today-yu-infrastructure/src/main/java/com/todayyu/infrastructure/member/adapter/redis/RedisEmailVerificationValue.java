package com.todayyu.infrastructure.member.adapter.redis;

import com.todayyu.domain.member.entity.EmailVerification;
import com.todayyu.domain.common.vo.Email;
import com.todayyu.domain.member.vo.VerificationCode;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class RedisEmailVerificationValue {
    private String code;
    private LocalDateTime issuedAt;
    
    @Builder
    public RedisEmailVerificationValue(String code, LocalDateTime issuedAt) {
        this.code = code;
        this.issuedAt = issuedAt;
    }
    
    public static RedisEmailVerificationValue from(EmailVerification verification) {
        return RedisEmailVerificationValue.builder()
                .code(verification.getCode().getValue())
                .issuedAt(verification.getIssuedAt())
                .build();
    }
    
    public EmailVerification toDomain(Email email) {
        return new EmailVerification(email, new VerificationCode(code), issuedAt);
    }
}
