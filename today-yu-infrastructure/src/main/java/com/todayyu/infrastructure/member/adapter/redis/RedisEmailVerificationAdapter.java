package com.todayyu.infrastructure.member.adapter.redis;

import com.todayyu.application.member.port.read.ReadEmailVerificationPort;
import com.todayyu.application.member.port.save.SaveEmailVerificationPort;
import com.todayyu.domain.member.entity.EmailVerification;
import com.todayyu.domain.member.vo.Email;
import com.todayyu.infrastructure.common.annotation.Adapter;
import lombok.RequiredArgsConstructor;

import java.time.Duration;
import java.util.Optional;

@Adapter
@RequiredArgsConstructor
public class RedisEmailVerificationAdapter implements ReadEmailVerificationPort, SaveEmailVerificationPort {
    private final RedisEmailVerificationStore emailVerificationStore;
    
    @Override
    public Optional<EmailVerification> readByEmail(Email email) {
        return emailVerificationStore.get(email.getValue())
                .map(value -> value.toEmailVerification(email));
    }
    
    @Override
    public void save(EmailVerification verification) {
        String email = verification.getEmail().getValue();
        RedisEmailVerificationValue value = RedisEmailVerificationValue.from(verification);
        Duration duration = Duration.between(verification.getIssuedAt(), verification.getExpiredAt());
        
        emailVerificationStore.save(email, value, duration);
    }
}
