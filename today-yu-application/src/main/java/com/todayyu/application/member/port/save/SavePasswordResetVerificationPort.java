package com.todayyu.application.member.port.save;

import com.todayyu.domain.member.entity.PasswordResetVerification;

public interface SavePasswordResetVerificationPort {
    PasswordResetVerification save(PasswordResetVerification verification);
}
