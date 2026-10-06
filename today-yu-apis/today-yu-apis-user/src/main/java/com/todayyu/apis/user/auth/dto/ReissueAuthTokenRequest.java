package com.todayyu.apis.user.auth.dto;

import com.todayyu.application.auth.dto.ReissueAuthTokenCommand;
import com.todayyu.domain.auth.vo.RefreshToken;
import jakarta.validation.constraints.NotBlank;

public record ReissueAuthTokenRequest(
        @NotBlank(message = "Refresh Token은 비어있을 수 없습니다.")
        String refreshToken
) {
    public ReissueAuthTokenCommand toCommand() {
        return new ReissueAuthTokenCommand(new RefreshToken(refreshToken));
    }
}
