package com.todayyu.domain.member.entity;

import com.todayyu.common.exception.CustomException;
import com.todayyu.domain.common.vo.Email;
import com.todayyu.domain.member.error.MemberErrorType;
import com.todayyu.domain.member.vo.EncodedPassword;
import com.todayyu.domain.member.vo.MemberId;
import com.todayyu.domain.member.vo.Nickname;
import com.todayyu.domain.member.vo.Role;
import com.todayyu.domain.member.vo.SocialIdentity;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Getter
public class Member {
    private MemberId id;
    private Email email;
    private Nickname nickname;
    private Role role;
    private LocalDateTime withdrawnAt;
    private List<Account> accounts;
    
    private Member(MemberId id, Email email, Nickname nickname, Role role, List<Account> accounts) {
        this.id = id;
        this.email = email;
        this.nickname = nickname;
        this.role = role;
        this.accounts = new ArrayList<>(accounts);
    }
    
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Member member)) return false;
        return Objects.equals(id, member.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
    
    public static Member withoutId(Email email, Nickname nickname, Role role, Account account) {
        if (account == null) {
            throw new IllegalArgumentException("회원은 하나 이상의 계정을 가져야합니다.");
        }
        
        return new Member(null, email, nickname, role, List.of(account));
    }
    
    public static Member withId(MemberId memberId, Email email, Nickname nickname, Role role, List<Account> accounts) {
        if (memberId == null) {
            throw new IllegalArgumentException("회원 아이디는 비어있을 수 없습니다.");
        }
        
        if (accounts == null || accounts.isEmpty()) {
            throw new IllegalArgumentException("회원은 하나 이상의 계정을 가져야합니다.");
        }
        
        return new Member(memberId, email, nickname, role, accounts);
    }
    
    /**
     * 회원의 암호화된 비밀번호({@link EncodedPassword})를 반환하는 메서드
     *
     * <p> 존재하지 않을 경우 `null`을 반환할 수 있으므로 {@link Optional}로 감싸 반환한다. </p>
     *
     * @return 암호회된 비밀번호
     */
    public Optional<EncodedPassword> getEncodedPassword() {
        return accounts.stream()
                .filter(LocalAccount.class::isInstance)
                .map(LocalAccount.class::cast)
                .map(LocalAccount::getPassword)
                .findFirst();
    }
    
    /**
     * 회원이 일반 로그인({@link LocalAccount}) 계정 보유 여부를 반환하는 메서드
     *
     * @return 일반 로그인 계정 보유 여부
     */
    public boolean hasLocalAccount() {
        return accounts.stream()
                .anyMatch(account -> LocalAccount.class.isInstance(account));
    }
    
    /**
     * 회원의 비밀번호를 변경하는 메서드
     *
     * <p> 회원이 로컬 계정({@link LocalAccount})을 가지고 있지 않은 경우 예외를 응답한다. </p>
     *
     * @param password 새 비밀번호
     * @throws IllegalStateException 로컬 계정이 존재하지 않는 경우
     */
    public void changePassword(EncodedPassword password) {
        LocalAccount localAccount = accounts.stream()
                .filter(LocalAccount.class::isInstance)
                .map(LocalAccount.class::cast)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("로컬 계정이 존재하지 않는 회원입니다."));
        
        localAccount.changePassword(password);
    }
    
    /**
     * 회원 탈퇴 메서드
     *
     * <p> 이미 탈퇴한 회원일 경우, 에러를 발생시킨다. </p>
     *
     * <p> 회원 정보를 비식별화를 진행하며, 탈퇴 시각을 기록한다. </p>
     *
     * @param time 탈퇴 시각
     * @throws CustomException {@link MemberErrorType#ALREADY_WITHDRAWN} - 회원이 이미 탈퇴한 경우
     */
    public void withdraw(LocalDateTime time) {
        if (isWithdrawn()) {
            throw new CustomException(MemberErrorType.ALREADY_WITHDRAWN);
        }
        
        anonymize();
        this.withdrawnAt = time;
    }
    
    /**
     * 회원의 탈퇴 여부 반환 메서드
     *
     * @return 탈퇴 여부
     */
    public boolean isWithdrawn() {
        return this.withdrawnAt != null;
    }
    
    /**
     * 회원 정보 익명화 메서드
     *
     * <p> 이메일과 연동된 계정을 제거한다. </p>
     */
    private void anonymize() {
        this.email = null;
        this.accounts.clear();
    }
    
    /**
     * 회원과 연동된 소셜 인증 정보 목록을 반환하는 메서드
     *
     * @return 소셜 인증 정보 목록
     */
    public List<SocialIdentity> getSocialIdentities() {
        return accounts.stream()
                .filter(SocialAccount.class::isInstance)
                .map(SocialAccount.class::cast)
                .map(SocialAccount::getSocialIdentity)
                .toList();
    }
}
