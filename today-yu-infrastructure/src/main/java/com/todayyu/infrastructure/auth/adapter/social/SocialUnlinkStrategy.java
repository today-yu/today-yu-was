package com.todayyu.infrastructure.auth.adapter.social;

import com.todayyu.domain.member.vo.AuthProvider;
import com.todayyu.domain.member.vo.AuthProviderUserId;

public interface SocialUnlinkStrategy {
    AuthProvider getAuthProvider();
    void unlink(AuthProviderUserId authProviderUserId);
}
