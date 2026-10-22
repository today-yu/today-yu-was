package com.todayyu.application.member.port.save;

import com.todayyu.domain.member.entity.PasswordResetVerification;

public interface SavePasswordResetVerificationPort {
    void save(PasswordResetVerification verification);
}
