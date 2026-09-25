package com.todayyu.domain.member.vo;

import lombok.Getter;

import java.util.Objects;

@Getter
public class SocialIdentity {
    private final AuthProvider authProvider;
    private final AuthProviderUserId authProviderUserId;

    public SocialIdentity(AuthProvider authProvider, AuthProviderUserId authProviderUserId) {
        if (authProvider == null || !authProvider.isSocial()) {
            throw new IllegalArgumentException("소셜 인증 제공자가 아닙니다. (%s)".formatted(authProvider));
        }

        if (authProviderUserId == null) {
            throw new IllegalArgumentException("인증 제공자에서의 사용자 아이디는 비어있을 수 없습니다.");
        }

        this.authProvider = authProvider;
        this.authProviderUserId = authProviderUserId;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof SocialIdentity that)) return false;
        return authProvider == that.authProvider
                && Objects.equals(authProviderUserId, that.authProviderUserId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(authProvider, authProviderUserId);
    }
}
