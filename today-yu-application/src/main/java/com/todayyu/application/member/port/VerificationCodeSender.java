package com.todayyu.application.member.port;

import com.todayyu.domain.member.entity.EmailVerification;

public interface VerificationCodeSender {
    void send(EmailVerification verification);
}
