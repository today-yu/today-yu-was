package com.todayyu.domain.member.entity;

import com.todayyu.domain.member.vo.AccountId;
import com.todayyu.domain.member.vo.AuthProvider;
import com.todayyu.domain.member.vo.EncodedPassword;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LocalAccountTest {
    private static final AccountId ACCOUNT_ID = new AccountId(1L);
    private static final EncodedPassword ENCODED_PASSWORD = new EncodedPassword("encoded-password");

    @Test
    @DisplayName("아이디가 없는 신규 일반 계정을 생성할 수 있다")
    void createWithoutId() {
        LocalAccount account = LocalAccount.withoutId(ENCODED_PASSWORD);

        assertNull(account.getId());
        assertEquals(ENCODED_PASSWORD, account.getPassword());
        assertEquals(AuthProvider.LOCAL, account.getAuthProvider());
    }

    @Test
    @DisplayName("저장된 계정 정보로 아이디를 가진 일반 계정을 생성할 수 있다")
    void createWithId() {
        LocalAccount account = LocalAccount.withId(ACCOUNT_ID, ENCODED_PASSWORD);

        assertEquals(ACCOUNT_ID, account.getId());
        assertEquals(ENCODED_PASSWORD, account.getPassword());
        assertEquals(AuthProvider.LOCAL, account.getAuthProvider());
    }

    @Test
    @DisplayName("아이디 없이 저장된 일반 계정을 생성할 수 없다")
    void createWithIdWithoutAccountId() {
        assertThrows(IllegalArgumentException.class,
                () -> LocalAccount.withId(null, ENCODED_PASSWORD));
    }
}
