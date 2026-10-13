package com.todayyu.persistence.member.mapper;

import com.todayyu.domain.member.entity.Account;
import com.todayyu.domain.member.entity.LocalAccount;
import com.todayyu.domain.member.entity.SocialAccount;
import com.todayyu.domain.member.vo.AccountId;
import com.todayyu.domain.member.vo.AuthProvider;
import com.todayyu.domain.member.vo.AuthProviderUserId;
import com.todayyu.domain.member.vo.EncodedPassword;
import com.todayyu.domain.member.vo.SocialIdentity;
import com.todayyu.persistence.member.entity.AccountJpaEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AccountMapperTest {
    private static final Long ACCOUNT_ID = 10L;
    private static final Long MEMBER_ID = 1L;
    private static final String ENCODED_PASSWORD = "encoded-password";
    private static final String AUTH_PROVIDER_USER_ID = "kakao-12345";

    private final AccountMapper accountMapper = new AccountMapper();

    @Test
    @DisplayName("일반 계정을 회원 아이디와 비밀번호를 가진 LOCAL 계정 JPA 엔티티로 변환한다")
    void localAccountToJpaEntity() {
        // given
        LocalAccount account = LocalAccount.withoutId(new EncodedPassword(ENCODED_PASSWORD));

        // when
        AccountJpaEntity jpaEntity = accountMapper.toJpaEntity(account, MEMBER_ID);

        // then
        assertNull(jpaEntity.getId());
        assertEquals(MEMBER_ID, jpaEntity.getMemberId());
        assertEquals(AuthProvider.LOCAL, jpaEntity.getAuthProvider());
        assertEquals(ENCODED_PASSWORD, jpaEntity.getPassword());
        assertNull(jpaEntity.getAuthProviderUserId());
    }

    @Test
    @DisplayName("소셜 계정을 회원 아이디와 인증 제공자 정보를 가진 계정 JPA 엔티티로 변환한다")
    void socialAccountToJpaEntity() {
        // given
        SocialAccount account = SocialAccount.withoutId(
                new SocialIdentity(AuthProvider.KAKAO, new AuthProviderUserId(AUTH_PROVIDER_USER_ID)));

        // when
        AccountJpaEntity jpaEntity = accountMapper.toJpaEntity(account, MEMBER_ID);

        // when
        assertNull(jpaEntity.getId());
        assertEquals(MEMBER_ID, jpaEntity.getMemberId());
        assertEquals(AuthProvider.KAKAO, jpaEntity.getAuthProvider());
        assertEquals(AUTH_PROVIDER_USER_ID, jpaEntity.getAuthProviderUserId());
        assertNull(jpaEntity.getPassword());
    }

    @Test
    @DisplayName("지원하지 않는 계정 타입은 계정 JPA 엔티티로 변환할 수 없다")
    void unsupportedAccountToJpaEntity() {
        // given
        Account unsupportedAccount = new Account() {
            @Override
            public AccountId getId() {
                return null;
            }

            @Override
            public AuthProvider getAuthProvider() {
                return AuthProvider.LOCAL;
            }
        };

        // when & then
        assertThrows(IllegalArgumentException.class,
                () -> accountMapper.toJpaEntity(unsupportedAccount, MEMBER_ID));
    }

    @Test
    @DisplayName("LOCAL 계정 JPA 엔티티를 아이디와 비밀번호를 가진 일반 계정으로 변환한다")
    void localJpaEntityToDomain() {
        // given
        AccountJpaEntity jpaEntity = AccountJpaEntity.builder()
                .memberId(MEMBER_ID)
                .authProvider(AuthProvider.LOCAL)
                .password(ENCODED_PASSWORD)
                .build();
        ReflectionTestUtils.setField(jpaEntity, "id", ACCOUNT_ID);

        // when
        Account account = accountMapper.toDomain(jpaEntity);

        // then
        LocalAccount localAccount = assertInstanceOf(LocalAccount.class, account);
        assertEquals(new AccountId(ACCOUNT_ID), localAccount.getId());
        assertEquals(new EncodedPassword(ENCODED_PASSWORD), localAccount.getPassword());
        assertEquals(AuthProvider.LOCAL, localAccount.getAuthProvider());
    }

    @Test
    @DisplayName("소셜 계정 JPA 엔티티를 아이디와 소셜 식별 정보를 가진 소셜 계정으로 변환한다")
    void socialJpaEntityToDomain() {
        // given
        AccountJpaEntity jpaEntity = AccountJpaEntity.builder()
                .memberId(MEMBER_ID)
                .authProvider(AuthProvider.KAKAO)
                .authProviderUserId(AUTH_PROVIDER_USER_ID)
                .build();
        ReflectionTestUtils.setField(jpaEntity, "id", ACCOUNT_ID);

        // when
        Account account = accountMapper.toDomain(jpaEntity);

        // then
        SocialAccount socialAccount = assertInstanceOf(SocialAccount.class, account);
        assertEquals(new AccountId(ACCOUNT_ID), socialAccount.getId());
        assertEquals(
                new SocialIdentity(AuthProvider.KAKAO, new AuthProviderUserId(AUTH_PROVIDER_USER_ID)),
                socialAccount.getSocialIdentity()
        );
        assertEquals(AuthProvider.KAKAO, socialAccount.getAuthProvider());
    }
}
