package com.todayyu.application.auth.port.save;

import com.todayyu.domain.auth.vo.TokenId;

import java.time.Instant;

public interface SaveBlacklistTokenPort {
    void save(TokenId tokenId, Instant expiresAt);
}
