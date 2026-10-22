package com.todayyu.infrastructure.member.adapter.redis;

import com.todayyu.domain.common.vo.Email;
import com.todayyu.domain.member.entity.PasswordResetVerification;
import com.todayyu.domain.member.vo.VerificationCode;
import com.todayyu.domain.member.vo.VerificationFailCount;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class RedisPasswordResetVerificationValue {
    private String email;
    private String code;
    private LocalDateTime issuedAt;
    private int failCount;
    
    @Builder
    public RedisPasswordResetVerificationValue(String email, String code, LocalDateTime issuedAt, int failCount) {
        this.email = email;
        this.code = code;
        this.issuedAt = issuedAt;
        this.failCount = failCount;
    }
    
    public static RedisPasswordResetVerificationValue from(PasswordResetVerification verification) {
        return new RedisPasswordResetVerificationValue(
                verification.getEmail().getValue(),
                verification.getCode().getValue(),
                verification.getIssuedAt(),
                verification.getFailCount().getValue()
        );
    }
    
    public PasswordResetVerification toDomain() {
        return PasswordResetVerification.of(
                new Email(email),
                new VerificationCode(code),
                issuedAt,
                VerificationFailCount.of(failCount)
        );
    }
}
