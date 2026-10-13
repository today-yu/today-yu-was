package com.todayyu.application.auth.dto.command;

import com.todayyu.domain.auth.vo.RefreshToken;

public record ReissueAuthTokenCommand(
        RefreshToken refreshToken
) {
}
