package com.todayyu.domain.member.entity;

import com.todayyu.domain.member.vo.AuthProvider;
import com.todayyu.domain.member.vo.AuthProviderUserId;
import com.todayyu.domain.member.vo.Email;
import com.todayyu.domain.member.vo.EncodedPassword;
import com.todayyu.domain.member.vo.Nickname;
import com.todayyu.domain.member.vo.Role;
import com.todayyu.domain.member.vo.SocialIdentity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MemberTest {

    private static final Email EMAIL = new Email("user@yu.ac.kr");
    private static final Nickname NICKNAME = new Nickname("영대생");

    @Test
    @DisplayName("일반 계정으로 회원을 생성할 수 있다")
    void createWithLocalAccount() {
        LocalAccount account = new LocalAccount(null, new EncodedPassword("encoded-password"));

        Member member = new Member(EMAIL, NICKNAME, account);

        assertEquals(EMAIL, member.getEmail());
        assertEquals(NICKNAME, member.getNickname());
        assertEquals(List.of(account), member.getAccounts());
        assertEquals(AuthProvider.LOCAL, member.getAccounts().get(0).getAuthProvider());
    }

    @Test
    @DisplayName("소셜 계정으로 회원을 생성할 수 있다")
    void createWithSocialAccount() {
        SocialIdentity identity = new SocialIdentity(AuthProvider.KAKAO, new AuthProviderUserId("kakao-12345"));
        SocialAccount account = new SocialAccount(null, identity);

        Member member = new Member(EMAIL, NICKNAME, account);

        assertEquals(EMAIL, member.getEmail());
        assertEquals(NICKNAME, member.getNickname());
        assertEquals(List.of(account), member.getAccounts());
        assertEquals(AuthProvider.KAKAO, member.getAccounts().get(0).getAuthProvider());
    }

    @Test
    @DisplayName("회원은 생성 시 일반 회원 역할을 가진다")
    void defaultRole() {
        LocalAccount account = new LocalAccount(null, new EncodedPassword("encoded-password"));

        Member member = new Member(EMAIL, NICKNAME, account);

        assertEquals(Role.MEMBER, member.getRole());
    }
}
