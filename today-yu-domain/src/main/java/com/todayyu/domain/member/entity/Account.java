package com.todayyu.domain.member.entity;

import com.todayyu.domain.member.vo.AccountId;
import com.todayyu.domain.member.vo.AuthProvider;

public interface Account {
    AccountId getId();
    AuthProvider getAuthProvider();
}
