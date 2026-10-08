package com.todayyu.persistence.info.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "helpful_mark")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HelpfulMarkJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "info_id", nullable = false)
    private Long infoId;
    
    @Column(name = "member_id", nullable = false)
    private Long memberId;
    
    @Builder
    public HelpfulMarkJpaEntity(Long infoId, Long memberId) {
        this.infoId = infoId;
        this.memberId = memberId;
    }
}
