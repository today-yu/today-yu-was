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
    
    private LocalAccount(AccountId id, EncodedPassword password) {
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
    
    public static LocalAccount withoutId(EncodedPassword password) {
        return new LocalAccount(null, password);
    }
    
    public static LocalAccount withId(AccountId id, EncodedPassword password) {
        if (id == null) {
            throw new IllegalArgumentException("계정 아이디는 비어있을 수 없습니다.");
        }
        return new LocalAccount(id, password);
    }
    
    /**
     * 비밀번호 변경 메서드
     *
     * @param password 새 비밀번호
     */
    public void changePassword(EncodedPassword password) {
        this.password = password;
    }
}
