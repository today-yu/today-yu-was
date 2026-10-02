package com.todayyu.domain.member.entity;

import com.todayyu.domain.member.vo.Email;
import com.todayyu.domain.member.vo.MemberId;
import com.todayyu.domain.member.vo.Nickname;
import com.todayyu.domain.member.vo.Role;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Getter
public class Member {
    private MemberId id;
    private Email email;
    private Nickname nickname;
    private Role role;
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
}
