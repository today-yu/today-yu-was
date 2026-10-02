package com.todayyu.persistence.member.mapper;

import com.todayyu.domain.member.entity.Account;
import com.todayyu.domain.member.entity.LocalAccount;
import com.todayyu.domain.member.entity.SocialAccount;
import com.todayyu.domain.member.vo.AccountId;
import com.todayyu.domain.member.vo.AuthProvider;
import com.todayyu.domain.member.vo.AuthProviderUserId;
import com.todayyu.domain.member.vo.EncodedPassword;
import com.todayyu.domain.member.vo.SocialIdentity;
import com.todayyu.persistence.common.Mapper;
import com.todayyu.persistence.member.entity.AccountJpaEntity;

@Mapper
public class AccountMapper {
    /**
     * {@link Account}를 {@link AccountJpaEntity}로 변환하는 메서드
     *
     * <p> 전달된 계정이 {@link LocalAccount}일 경우, {@link #toJpaEntity(LocalAccount, Long)}를 호출한다. </p>
     * <p> 전달된 계정이 {@link SocialAccount}일 경우, {@link #toJpaEntity(SocialAccount, Long)}를 호출한다. </p>
     *
     * @param account {@link Account} 객체
     * @param memberId 계정의 주인(회원) 아이디
     * @return {@link AccountJpaEntity} 객체
     */
    public AccountJpaEntity toJpaEntity(Account account, Long memberId) {
        if (account instanceof LocalAccount localAccount) {
            return toJpaEntity(localAccount, memberId);
        } else if (account instanceof SocialAccount socialAccount) {
            return toJpaEntity(socialAccount, memberId);
        }
        
        throw new IllegalArgumentException("지원하지 않는 계정 타입입니다. Account=" + account.getClass());
    }
    
    /**
     * {@link LocalAccount}를 {@link AccountJpaEntity}로 변환하는 메서드
     *
     * @param account {@link LocalAccount} 객체
     * @param memberId 계정의 주인(회원) 아이디
     * @return {@link AccountJpaEntity} 객체
     */
    private AccountJpaEntity toJpaEntity(LocalAccount account, Long memberId) {
        return AccountJpaEntity.builder()
                .memberId(memberId)
                .authProvider(account.getAuthProvider())
                .password(account.getPassword().getValue())
                .build();
    }

    /**
     * {@link SocialAccount}를 {@link AccountJpaEntity}로 변환하는 메서드
     *
     * @param account {@link SocialAccount} 객체
     * @param memberId 계정의 주인(회원) 아이디
     * @return {@link AccountJpaEntity} 객체
     */
    private AccountJpaEntity toJpaEntity(SocialAccount account, Long memberId) {
        return AccountJpaEntity.builder()
                .memberId(memberId)
                .authProvider(account.getAuthProvider())
                .authProviderUserId(account.getSocialIdentity().getAuthProviderUserId().getValue())
                .build();
    }
    
    /**
     * {@link AccountJpaEntity}를 {@link Account}로 변환하는 메서드
     *
     * <p> {@link AccountJpaEntity}의 소셜 계정 여부를 {@link AuthProvider#isSocial()}를 호출하여 판단한다. </p>
     *
     * <p> 소셜 계정일 경우, {@link #toSocialAccount(AccountJpaEntity)}를 호출하여 {@link SocialAccount} 객체로 변환한다. </p>
     * <p> 로컬 계정일 경우, {@link #toLocalAccount(AccountJpaEntity)}를 호출하여 {@link LocalAccount} 객체로 변환한다. </p>
     *
     * @param accountJpaEntity {@link AccountJpaEntity} 객체
     * @return {@link Account} 객체
     */
    public Account toDomain(AccountJpaEntity accountJpaEntity) {
        AuthProvider authProvider = accountJpaEntity.getAuthProvider();
        
        if (authProvider.isSocial()) {
            return toSocialAccount(accountJpaEntity);
        } else {
            return toLocalAccount(accountJpaEntity);
        }
    }
    
    /**
     * {@link AccountJpaEntity}를 {@link LocalAccount}로 변환하는 메서드
     *
     * @param account {@link AccountJpaEntity} 객체
     * @return {@link LocalAccount} 객체
     */
    private LocalAccount toLocalAccount(AccountJpaEntity account) {
        return LocalAccount.withId(new AccountId(account.getId()), new EncodedPassword(account.getPassword()));
    }
    
    /**
     * {@link AccountJpaEntity}를 {@link SocialAccount}로 변환하는 메서드
     *
     * @param account {@link AccountJpaEntity} 객체
     * @return {@link SocialAccount} 객체
     */
    private SocialAccount toSocialAccount(AccountJpaEntity account) {
        return SocialAccount.withId(
                new AccountId(account.getId()),
                new SocialIdentity(account.getAuthProvider(), new AuthProviderUserId(account.getAuthProviderUserId()))
        );
    }
}
