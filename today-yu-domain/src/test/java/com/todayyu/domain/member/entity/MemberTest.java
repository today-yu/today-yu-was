package com.todayyu.domain.member.entity;

import com.todayyu.domain.member.vo.AccountId;
import com.todayyu.domain.member.vo.AuthProvider;
import com.todayyu.domain.member.vo.AuthProviderUserId;
import com.todayyu.domain.member.vo.Email;
import com.todayyu.domain.member.vo.EncodedPassword;
import com.todayyu.domain.member.vo.MemberId;
import com.todayyu.domain.member.vo.Nickname;
import com.todayyu.domain.member.vo.Role;
import com.todayyu.domain.member.vo.SocialIdentity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MemberTest {

    private static final MemberId MEMBER_ID = new MemberId(1L);
    private static final Email EMAIL = new Email("user@yu.ac.kr");
    private static final Nickname NICKNAME = new Nickname("영대생");
    private static final EncodedPassword ENCODED_PASSWORD = new EncodedPassword("encoded-password");

    @Test
    @DisplayName("일반 계정으로 아이디가 없는 신규 회원을 생성할 수 있다")
    void createWithLocalAccount() {
        // given
        LocalAccount account = LocalAccount.withoutId(ENCODED_PASSWORD);

        // when
        Member member = Member.withoutId(EMAIL, NICKNAME, Role.MEMBER, account);

        // then
        assertNull(member.getId());
        assertEquals(EMAIL, member.getEmail());
        assertEquals(NICKNAME, member.getNickname());
        assertEquals(Role.MEMBER, member.getRole());
        assertEquals(List.of(account), member.getAccounts());
        assertEquals(AuthProvider.LOCAL, member.getAccounts().get(0).getAuthProvider());
    }

    @Test
    @DisplayName("소셜 계정으로 아이디가 없는 신규 회원을 생성할 수 있다")
    void createWithSocialAccount() {
        // given
        SocialIdentity identity = new SocialIdentity(AuthProvider.KAKAO, new AuthProviderUserId("kakao-12345"));
        SocialAccount account = SocialAccount.withoutId(identity);

        // when
        Member member = Member.withoutId(EMAIL, NICKNAME, Role.MEMBER, account);

        // then
        assertNull(member.getId());
        assertEquals(EMAIL, member.getEmail());
        assertEquals(NICKNAME, member.getNickname());
        assertEquals(Role.MEMBER, member.getRole());
        assertEquals(List.of(account), member.getAccounts());
        assertEquals(AuthProvider.KAKAO, member.getAccounts().get(0).getAuthProvider());
    }

    @Test
    @DisplayName("계정 없이 신규 회원을 생성할 수 없다")
    void createWithoutAccount() {
        assertThrows(IllegalArgumentException.class,
                () -> Member.withoutId(EMAIL, NICKNAME, Role.MEMBER, null));
    }

    @Test
    @DisplayName("저장된 회원 정보로 아이디와 계정 목록을 가진 회원을 생성할 수 있다")
    void createWithId() {
        // given
        LocalAccount account = LocalAccount.withId(new AccountId(10L), ENCODED_PASSWORD);

        // when
        Member member = Member.withId(MEMBER_ID, EMAIL, NICKNAME, Role.MEMBER, List.of(account));

        // then
        assertEquals(MEMBER_ID, member.getId());
        assertEquals(EMAIL, member.getEmail());
        assertEquals(NICKNAME, member.getNickname());
        assertEquals(Role.MEMBER, member.getRole());
        assertEquals(List.of(account), member.getAccounts());
    }

    @Test
    @DisplayName("아이디 없이 저장된 회원을 생성할 수 없다")
    void createWithIdWithoutMemberId() {
        LocalAccount account = LocalAccount.withId(new AccountId(10L), ENCODED_PASSWORD);

        assertThrows(IllegalArgumentException.class,
                () -> Member.withId(null, EMAIL, NICKNAME, Role.MEMBER, List.of(account)));
    }

    @Test
    @DisplayName("계정 목록이 없거나 비어 있으면 저장된 회원을 생성할 수 없다")
    void createWithIdWithoutAccounts() {
        assertThrows(IllegalArgumentException.class,
                () -> Member.withId(MEMBER_ID, EMAIL, NICKNAME, Role.MEMBER, null));
        assertThrows(IllegalArgumentException.class,
                () -> Member.withId(MEMBER_ID, EMAIL, NICKNAME, Role.MEMBER, List.of()));
    }

    @Test
    @DisplayName("회원 생성 후 전달한 계정 목록을 변경해도 회원의 계정 목록은 변경되지 않는다")
    void copyAccounts() {
        // given
        LocalAccount account = LocalAccount.withId(new AccountId(10L), ENCODED_PASSWORD);
        List<Account> accounts = new ArrayList<>(List.of(account));

        // when
        Member member = Member.withId(MEMBER_ID, EMAIL, NICKNAME, Role.MEMBER, accounts);
        accounts.add(LocalAccount.withId(new AccountId(11L), ENCODED_PASSWORD));

        // then
        assertEquals(List.of(account), member.getAccounts());
    }
}
