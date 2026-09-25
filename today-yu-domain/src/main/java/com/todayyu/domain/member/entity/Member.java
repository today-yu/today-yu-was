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
    
    public Member(Email email, Nickname nickname, Account account) {
        this.email = email;
        this.nickname = nickname;
        this.role = Role.MEMBER;
        this.accounts = new ArrayList<>(List.of(account));
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
}
