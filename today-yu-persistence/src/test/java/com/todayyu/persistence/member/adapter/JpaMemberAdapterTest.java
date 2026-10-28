package com.todayyu.persistence.member.adapter;

import com.todayyu.domain.common.vo.Email;
import com.todayyu.domain.member.entity.Account;
import com.todayyu.domain.member.entity.LocalAccount;
import com.todayyu.domain.member.entity.Member;
import com.todayyu.domain.member.entity.SocialAccount;
import com.todayyu.domain.member.vo.AccountId;
import com.todayyu.domain.member.vo.AuthProvider;
import com.todayyu.domain.member.vo.AuthProviderUserId;
import com.todayyu.domain.member.vo.EncodedPassword;
import com.todayyu.domain.member.vo.MemberId;
import com.todayyu.domain.member.vo.Nickname;
import com.todayyu.domain.member.vo.Role;
import com.todayyu.domain.member.vo.SocialIdentity;
import com.todayyu.persistence.member.entity.AccountJpaEntity;
import com.todayyu.persistence.member.entity.MemberJpaEntity;
import com.todayyu.persistence.member.mapper.AccountMapper;
import com.todayyu.persistence.member.mapper.MemberMapper;
import com.todayyu.persistence.member.repository.AccountJpaRepository;
import com.todayyu.persistence.member.repository.MemberJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class JpaMemberAdapterTest {
    private static final Long MEMBER_ID = 1L;
    private static final Long LOCAL_ACCOUNT_ID = 10L;
    private static final Long SOCIAL_ACCOUNT_ID = 11L;
    private static final String EMAIL = "user@yu.ac.kr";
    private static final String NICKNAME = "착한 영대인 1";
    private static final String PASSWORD = "encoded-password";
    private static final String NEW_PASSWORD = "encoded-new-password";
    private static final String AUTH_PROVIDER_USER_ID = "kakao-12345";

    @Mock
    private MemberJpaRepository memberRepository;

    @Mock
    private AccountJpaRepository accountRepository;

    @Spy
    private MemberMapper memberMapper;

    @Spy
    private AccountMapper accountMapper;

    @InjectMocks
    private JpaMemberAdapter memberAdapter;

    @Test
    @DisplayName("영속 상태의 계정 JPA 엔티티를 조회해 로컬 계정의 비밀번호를 변경하고, 도메인 엔티티로 변환해 반환한다")
    void update() {
        // given
        MemberJpaEntity memberJpaEntity = memberJpaEntity();
        AccountJpaEntity localAccountJpaEntity = localAccountJpaEntity();
        Member member = member(List.of(
                LocalAccount.withId(new AccountId(LOCAL_ACCOUNT_ID), new EncodedPassword(NEW_PASSWORD))
        ));

        given(memberRepository.findById(MEMBER_ID)).willReturn(Optional.of(memberJpaEntity));
        given(accountRepository.findByMemberId(MEMBER_ID)).willReturn(List.of(localAccountJpaEntity));

        // when
        Member result = memberAdapter.update(member);

        // then
        assertEquals(NEW_PASSWORD, localAccountJpaEntity.getPassword());
        assertEquals(new MemberId(MEMBER_ID), result.getId());
        assertEquals(Optional.of(new EncodedPassword(NEW_PASSWORD)), result.getEncodedPassword());
        then(memberRepository).should(never()).save(any());
        then(accountRepository).should(never()).saveAll(any());
    }

    @Test
    @DisplayName("로컬 계정과 소셜 계정을 가진 회원이면 로컬 계정의 비밀번호만 변경하고 소셜 계정은 변경하지 않는다")
    void updateWithSocialAccount() {
        // given
        MemberJpaEntity memberJpaEntity = memberJpaEntity();
        AccountJpaEntity localAccountJpaEntity = localAccountJpaEntity();
        AccountJpaEntity socialAccountJpaEntity = socialAccountJpaEntity();
        SocialIdentity identity = new SocialIdentity(AuthProvider.KAKAO, new AuthProviderUserId(AUTH_PROVIDER_USER_ID));
        Member member = member(List.of(
                LocalAccount.withId(new AccountId(LOCAL_ACCOUNT_ID), new EncodedPassword(NEW_PASSWORD)),
                SocialAccount.withId(new AccountId(SOCIAL_ACCOUNT_ID), identity)
        ));

        given(memberRepository.findById(MEMBER_ID)).willReturn(Optional.of(memberJpaEntity));
        given(accountRepository.findByMemberId(MEMBER_ID))
                .willReturn(List.of(localAccountJpaEntity, socialAccountJpaEntity));

        // when
        memberAdapter.update(member);

        // then
        assertEquals(NEW_PASSWORD, localAccountJpaEntity.getPassword());
        assertNull(socialAccountJpaEntity.getPassword());
        assertEquals(AUTH_PROVIDER_USER_ID, socialAccountJpaEntity.getAuthProviderUserId());
    }

    @Test
    @DisplayName("회원 ID에 해당하는 회원이 존재하지 않으면 예외가 발생하고 계정을 조회하지 않는다")
    void updateWithNotExistingMember() {
        // given
        Member member = member(List.of(
                LocalAccount.withId(new AccountId(LOCAL_ACCOUNT_ID), new EncodedPassword(NEW_PASSWORD))
        ));
        given(memberRepository.findById(MEMBER_ID)).willReturn(Optional.empty());

        // when & then
        assertThrows(IllegalStateException.class, () -> memberAdapter.update(member));
        then(accountRepository).should(never()).findByMemberId(any());
    }

    @Test
    @DisplayName("회원의 계정 ID에 해당하는 계정이 존재하지 않으면 예외가 발생한다")
    void updateWithNotExistingAccount() {
        // given
        Member member = member(List.of(
                LocalAccount.withId(new AccountId(LOCAL_ACCOUNT_ID), new EncodedPassword(NEW_PASSWORD))
        ));
        given(memberRepository.findById(MEMBER_ID)).willReturn(Optional.of(memberJpaEntity()));
        given(accountRepository.findByMemberId(MEMBER_ID)).willReturn(List.of());

        // when & then
        assertThrows(IllegalStateException.class, () -> memberAdapter.update(member));
    }

    private Member member(List<Account> accounts) {
        return Member.withId(new MemberId(MEMBER_ID), new Email(EMAIL), new Nickname(NICKNAME), Role.MEMBER, accounts);
    }

    private MemberJpaEntity memberJpaEntity() {
        MemberJpaEntity jpaEntity = MemberJpaEntity.builder()
                .email(EMAIL)
                .nickname(NICKNAME)
                .role(Role.MEMBER)
                .build();
        ReflectionTestUtils.setField(jpaEntity, "id", MEMBER_ID);
        return jpaEntity;
    }

    private AccountJpaEntity localAccountJpaEntity() {
        AccountJpaEntity jpaEntity = AccountJpaEntity.builder()
                .memberId(MEMBER_ID)
                .authProvider(AuthProvider.LOCAL)
                .password(PASSWORD)
                .build();
        ReflectionTestUtils.setField(jpaEntity, "id", LOCAL_ACCOUNT_ID);
        return jpaEntity;
    }

    private AccountJpaEntity socialAccountJpaEntity() {
        AccountJpaEntity jpaEntity = AccountJpaEntity.builder()
                .memberId(MEMBER_ID)
                .authProvider(AuthProvider.KAKAO)
                .authProviderUserId(AUTH_PROVIDER_USER_ID)
                .build();
        ReflectionTestUtils.setField(jpaEntity, "id", SOCIAL_ACCOUNT_ID);
        return jpaEntity;
    }
}
