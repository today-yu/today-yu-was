package com.todayyu.application.member.port;

import com.todayyu.domain.member.entity.PasswordResetVerification;

public interface SendPasswordResetVerificationPort {
    void send(PasswordResetVerification verification);
}
