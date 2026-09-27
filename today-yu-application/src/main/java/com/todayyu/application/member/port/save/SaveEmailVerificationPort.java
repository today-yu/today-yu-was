package com.todayyu.application.member.port.save;

import com.todayyu.domain.member.entity.EmailVerification;

public interface SaveEmailVerificationPort {
    void save(EmailVerification emailVerification);
}
