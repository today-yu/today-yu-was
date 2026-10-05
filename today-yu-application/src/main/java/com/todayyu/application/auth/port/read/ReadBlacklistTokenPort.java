package com.todayyu.application.auth.port.read;

import com.todayyu.domain.auth.vo.TokenId;

public interface ReadBlacklistTokenPort {
    boolean isBlacklistToken(TokenId tokenId);
}
