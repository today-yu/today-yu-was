package com.todayyu.application.auth.dto;

import com.todayyu.domain.auth.vo.RefreshToken;

public record ReissueAuthTokenCommand(
        RefreshToken refreshToken
) {
}
