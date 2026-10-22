package com.todayyu.application.member.port;

import com.todayyu.domain.member.entity.EmailVerification;

public interface SendEmailVerificationPort {
    void send(EmailVerification verification);
}
