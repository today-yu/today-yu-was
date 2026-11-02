package com.todayyu.application.auth.port;

import com.todayyu.domain.member.vo.AuthProvider;
import com.todayyu.domain.member.vo.SocialIdentity;

public interface SocialAuthPort {
    SocialIdentity authenticate(AuthProvider provider, String authorizationCode);
}
