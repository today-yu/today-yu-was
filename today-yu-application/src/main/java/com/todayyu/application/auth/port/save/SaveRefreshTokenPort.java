package com.todayyu.application.auth.port.save;

import com.todayyu.application.auth.dto.IssuedRefreshToken;
import com.todayyu.domain.member.vo.MemberId;

public interface SaveRefreshTokenPort {
    void save(MemberId memberId, IssuedRefreshToken issuedRefreshToken);
}
