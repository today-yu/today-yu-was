package com.todayyu.apis.common.security.principal;

import com.todayyu.domain.member.vo.MemberId;
import com.todayyu.domain.member.vo.Role;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class CustomUserDetails implements UserDetails {
    private final MemberId memberId;
    private final Role role;
    
    public CustomUserDetails(MemberId memberId, Role role) {
        this.memberId = memberId;
        this.role = role;
    }
    
    public MemberId getMemberId() {
        return memberId;
    }
    
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }
    
    @Override
    public @Nullable String getPassword() {
        return "";
    }
    @Override
    public String getUsername() {
        return String.valueOf(memberId.getValue());
    }
}
