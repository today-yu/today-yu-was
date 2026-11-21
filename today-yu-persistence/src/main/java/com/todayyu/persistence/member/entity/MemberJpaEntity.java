package com.todayyu.persistence.member.entity;

import com.todayyu.domain.member.vo.Role;
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

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "member")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "email", unique = true)
    private String email;
    
    @Column(name = "nickname", nullable = false, unique = true)
    private String nickname;
    
    @Column(name = "withdrawn_at")
    private LocalDateTime withdrawnAt;
    
    @Column(name = "role", nullable = false)
    @Enumerated(value = EnumType.STRING)
    private Role role;
    
    @Builder
    public MemberJpaEntity(String email, String nickname, Role role) {
        this.email = email;
        this.nickname = nickname;
        this.role = role;
    }
    
    /**
     * 회원 탈퇴 메서드
     *
     * <p> 탈퇴 일시를 기록하고, 이메일을 제거하여 비식별화한다. </p>
     *
     * @param time 탈퇴 일시
     */
    public void withdraw(LocalDateTime time) {
        this.withdrawnAt = time;
        this.email = null;
    }
}
