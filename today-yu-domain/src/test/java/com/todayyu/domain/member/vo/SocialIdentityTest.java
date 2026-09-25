package com.todayyu.domain.member.vo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SocialIdentityTest {

    private static final AuthProviderUserId USER_ID = new AuthProviderUserId("kakao-12345");

    @Test
    @DisplayName("소셜 인증 제공자와 사용자 아이디로 생성할 수 있다")
    void create() {
        SocialIdentity identity = new SocialIdentity(AuthProvider.KAKAO, USER_ID);

        assertEquals(AuthProvider.KAKAO, identity.getAuthProvider());
        assertEquals(USER_ID, identity.getAuthProviderUserId());
    }

    @Test
    @DisplayName("일반(LOCAL) 인증 제공자로는 생성할 수 없다")
    void localProvider() {
        assertThrows(IllegalArgumentException.class, () -> new SocialIdentity(AuthProvider.LOCAL, USER_ID));
    }

    @Test
    @DisplayName("인증 제공자가 없으면 생성할 수 없다")
    void nullProvider() {
        assertThrows(IllegalArgumentException.class, () -> new SocialIdentity(null, USER_ID));
    }

    @Test
    @DisplayName("인증 제공자에서의 사용자 아이디가 없으면 생성할 수 없다")
    void nullUserId() {
        assertThrows(IllegalArgumentException.class, () -> new SocialIdentity(AuthProvider.KAKAO, null));
    }

    @Test
    @DisplayName("인증 제공자와 사용자 아이디가 같으면 같은 소셜 식별 정보이다")
    void equalsByValue() {
        SocialIdentity identity = new SocialIdentity(AuthProvider.KAKAO, new AuthProviderUserId("kakao-12345"));
        SocialIdentity other = new SocialIdentity(AuthProvider.KAKAO, new AuthProviderUserId("kakao-12345"));

        assertEquals(other, identity);
        assertEquals(other.hashCode(), identity.hashCode());
    }

    @Test
    @DisplayName("사용자 아이디가 다르면 다른 소셜 식별 정보이다")
    void notEqualsByUserId() {
        SocialIdentity identity = new SocialIdentity(AuthProvider.KAKAO, new AuthProviderUserId("kakao-12345"));
        SocialIdentity other = new SocialIdentity(AuthProvider.KAKAO, new AuthProviderUserId("kakao-67890"));

        assertNotEquals(other, identity);
    }
}
