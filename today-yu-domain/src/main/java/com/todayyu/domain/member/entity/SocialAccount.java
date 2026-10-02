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
    
    private SocialAccount(AccountId id, SocialIdentity socialIdentity) {
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
    
    public static SocialAccount withoutId(SocialIdentity socialIdentity) {
        return new SocialAccount(null, socialIdentity);
    }
    
    public static SocialAccount withId(AccountId id, SocialIdentity socialIdentity) {
        if (id == null) {
            throw new IllegalArgumentException("계정 아이디는 비어있을 수 없습니다.");
        }
        
        return new SocialAccount(id, socialIdentity);
    }
}
