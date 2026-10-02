package com.todayyu.persistence.member.entity;

import com.todayyu.domain.member.vo.AuthProvider;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "account")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AccountJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "auth_provider", nullable = false)
    @Enumerated(value = EnumType.STRING)
    private AuthProvider authProvider;
    
    @Column(name = "password")
    private String password;
    
    @Column(name = "auth_provider_user_id")
    private String authProviderUserId;
    
    @Column(name = "member_id", nullable = false)
    private Long memberId;
    
    @Builder
    public AccountJpaEntity(Long id, AuthProvider authProvider, String password, String authProviderUserId, Long memberId) {
        this.id = id;
        this.authProvider = authProvider;
        this.password = password;
        this.authProviderUserId = authProviderUserId;
        this.memberId = memberId;
    }
}
