package com.todayyu.application.member.port.read;

import com.todayyu.domain.common.vo.Email;
import com.todayyu.domain.member.entity.PasswordResetVerification;

import java.util.Optional;

public interface ReadPasswordResetVerificationPort {
    Optional<PasswordResetVerification> readByEmail(Email email);
}
