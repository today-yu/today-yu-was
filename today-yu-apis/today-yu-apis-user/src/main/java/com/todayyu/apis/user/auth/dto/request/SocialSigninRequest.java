package com.todayyu.apis.user.auth.dto.request;

import com.todayyu.application.auth.dto.command.SocialSigninCommand;
import jakarta.validation.constraints.NotBlank;

public record SocialSigninRequest(
        @NotBlank(message = "인가 코드는 비어있을 수 없습니다.")
        String authorizationCode
) {
    public SocialSigninCommand toCommand(AuthProviderParam provider) {
        return new SocialSigninCommand(provider.toDomain(), authorizationCode);
    }
}
