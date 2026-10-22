package com.todayyu.infrastructure.member.adapter.redis;

import com.todayyu.application.member.port.read.ReadPasswordResetVerificationPort;
import com.todayyu.application.member.port.save.SavePasswordResetVerificationPort;
import com.todayyu.domain.common.vo.Email;
import com.todayyu.domain.member.entity.PasswordResetVerification;
import com.todayyu.infrastructure.common.annotation.Adapter;
import lombok.RequiredArgsConstructor;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

@Adapter
@RequiredArgsConstructor
public class RedisPasswordResetVerificationAdapter
        implements ReadPasswordResetVerificationPort, SavePasswordResetVerificationPort {
    private final RedisPasswordResetVerificationStore passwordResetVerificationStore;
    
    /**
     * Redis에서 비밀번호 재설정 인증을 조회하는 메서드
     *
     * @param email 비밀번호 재설정 인증을 발급한 이메일
     * @return 비밀번호 재설정 인증, 만료되었거나 발급 이력이 없는 경우 빈 값
     */
    @Override
    public Optional<PasswordResetVerification> readByEmail(Email email) {
        return passwordResetVerificationStore.get(email.getValue())
                .map(RedisPasswordResetVerificationValue::toDomain);
    }
    
    /**
     * Redis에 비밀번호 재설정 인증을 저장하는 메서드
     *
     * <p> TTL은 현재 시각부터 인증 만료 시각({@link PasswordResetVerification#getExpiredAt()})까지 남은 시간으로 설정한다.
     *     검증 실패 횟수 갱신 등으로 다시 저장하더라도 만료 시각이 연장되지 않는다. </p>
     *
     * <p> 이미 만료된 인증은 저장하지 않는다. (Redis는 0 이하의 TTL을 허용하지 않음) </p>
     *
     * @param verification 저장할 비밀번호 재설정 인증
     */
    @Override
    public void save(PasswordResetVerification verification) {
        Duration ttl = Duration.between(LocalDateTime.now(), verification.getExpiredAt());
        
        if (ttl.isNegative() || ttl.isZero()) {
            return;
        }
        
        passwordResetVerificationStore.save(
                verification.getEmail().getValue(),
                RedisPasswordResetVerificationValue.from(verification),
                ttl
        );
    }
}
