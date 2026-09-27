package com.todayyu.application.member.port.read;

import com.todayyu.domain.member.entity.EmailVerification;
import com.todayyu.domain.member.vo.Email;

import java.util.Optional;

public interface ReadEmailVerificationPort {
    Optional<EmailVerification> readByEmail(Email email);
}
