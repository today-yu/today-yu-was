package com.todayyu.persistence.scrap.entity;

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

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "scrap")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ScrapJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "info_id", nullable = false)
    private Long infoId;
    
    @Column(name = "member_id", nullable = false)
    private Long memberId;
    
    @Column(name = "scrapped_at", nullable = false)
    private LocalDateTime scrappedAt;
    
    @Builder
    public ScrapJpaEntity(Long infoId, Long memberId, LocalDateTime scrappedAt) {
        this.infoId = infoId;
        this.memberId = memberId;
        this.scrappedAt = scrappedAt;
    }
}
