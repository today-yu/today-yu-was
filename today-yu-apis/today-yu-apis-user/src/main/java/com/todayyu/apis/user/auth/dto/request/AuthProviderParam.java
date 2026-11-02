package com.todayyu.apis.user.auth.dto.request;

import com.todayyu.domain.member.vo.AuthProvider;

public enum AuthProviderParam {
    KAKAO;
    
    public AuthProvider toDomain() {
        return switch (this) {
            case KAKAO -> AuthProvider.KAKAO;
        };
    }
}
