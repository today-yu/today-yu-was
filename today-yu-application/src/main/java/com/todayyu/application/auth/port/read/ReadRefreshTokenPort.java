package com.todayyu.application.auth.port.read;

import com.todayyu.domain.auth.vo.RefreshToken;
import com.todayyu.domain.auth.vo.SessionId;
import com.todayyu.domain.member.vo.MemberId;

import java.util.Optional;

public interface ReadRefreshTokenPort {
    Optional<RefreshToken> read(MemberId memberId, SessionId sessionId);
}
