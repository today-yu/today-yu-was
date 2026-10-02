package com.todayyu.persistence.member.entity;

import com.todayyu.domain.member.vo.NicknameModifier;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "nickname_sequence")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NicknameSequenceJpaEntity {
    @Id
    @Enumerated(value = EnumType.STRING)
    @Column(name = "modifier")
    private NicknameModifier modifier;
    
    @Column(name = "last_number", nullable = false)
    private Long lastNumber;
}
