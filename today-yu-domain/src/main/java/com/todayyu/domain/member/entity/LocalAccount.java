package com.todayyu.domain.member.entity;

import com.todayyu.domain.member.vo.AccountId;
import com.todayyu.domain.member.vo.AuthProvider;
import com.todayyu.domain.member.vo.EncodedPassword;
import lombok.Getter;

import java.util.Objects;

@Getter
public class LocalAccount implements Account {
    private AccountId id;
    
    private EncodedPassword password;
    
    private AuthProvider authProvider;
    
    public LocalAccount(AccountId id, EncodedPassword password) {
        this.id = id;
        this.password = password;
        this.authProvider = AuthProvider.LOCAL;
    }
    
    @Override
    public AccountId getId() {
        return id;
    }
    
    @Override
    public AuthProvider getAuthProvider() {
        return authProvider;
    }
    
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof LocalAccount that)) return false;
        return Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
