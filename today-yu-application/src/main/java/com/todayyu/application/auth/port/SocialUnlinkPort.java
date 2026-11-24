package com.todayyu.application.auth.port;

import com.todayyu.domain.member.vo.SocialIdentity;

public interface SocialUnlinkPort {
    void unlink(SocialIdentity socialIdentity);
}
