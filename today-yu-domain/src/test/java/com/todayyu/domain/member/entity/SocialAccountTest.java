package com.todayyu.domain.member.entity;

import com.todayyu.domain.member.vo.AccountId;
import com.todayyu.domain.member.vo.AuthProvider;
import com.todayyu.domain.member.vo.AuthProviderUserId;
import com.todayyu.domain.member.vo.SocialIdentity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SocialAccountTest {
    private static final AccountId ACCOUNT_ID = new AccountId(1L);
    private static final SocialIdentity SOCIAL_IDENTITY =
            new SocialIdentity(AuthProvider.KAKAO, new AuthProviderUserId("kakao-12345"));

    @Test
    @DisplayName("아이디가 없는 신규 소셜 계정을 생성할 수 있다")
    void createWithoutId() {
        SocialAccount account = SocialAccount.withoutId(SOCIAL_IDENTITY);

        assertNull(account.getId());
        assertEquals(SOCIAL_IDENTITY, account.getSocialIdentity());
        assertEquals(AuthProvider.KAKAO, account.getAuthProvider());
    }

    @Test
    @DisplayName("저장된 계정 정보로 아이디를 가진 소셜 계정을 생성할 수 있다")
    void createWithId() {
        SocialAccount account = SocialAccount.withId(ACCOUNT_ID, SOCIAL_IDENTITY);

        assertEquals(ACCOUNT_ID, account.getId());
        assertEquals(SOCIAL_IDENTITY, account.getSocialIdentity());
        assertEquals(AuthProvider.KAKAO, account.getAuthProvider());
    }

    @Test
    @DisplayName("아이디 없이 저장된 소셜 계정을 생성할 수 없다")
    void createWithIdWithoutAccountId() {
        assertThrows(IllegalArgumentException.class,
                () -> SocialAccount.withId(null, SOCIAL_IDENTITY));
    }
}
