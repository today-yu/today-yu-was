package com.todayyu.domain.member.entity;

import com.todayyu.domain.member.vo.AccountId;
import com.todayyu.domain.member.vo.AuthProvider;
import com.todayyu.domain.member.vo.SocialIdentity;
import lombok.Getter;

import java.util.Objects;

@Getter
public class SocialAccount implements Account {
    private AccountId id;
    
    private SocialIdentity socialIdentity;
    
    public SocialAccount(AccountId id, SocialIdentity socialIdentity) {
        this.id = id;
        this.socialIdentity = socialIdentity;
    }
    
    @Override
    public AccountId getId() {
        return id;
    }
    
    @Override
    public AuthProvider getAuthProvider() {
        return socialIdentity.getAuthProvider();
    }
    
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof SocialAccount that)) return false;
        return Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
