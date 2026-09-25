package com.todayyu.domain.member.vo;

public enum AuthProvider {
    LOCAL,
    KAKAO,
    ;
    
    public boolean isSocial() {
        return this != LOCAL;
    }
}
