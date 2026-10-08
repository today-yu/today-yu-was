package com.todayyu.persistence.info.entity;

import com.todayyu.domain.info.vo.InfoCategory;
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
@Table(name = "info")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InfoJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "category", nullable = false)
    @Enumerated(value = EnumType.STRING)
    private InfoCategory category;
    
    @Column(name = "title", nullable = false)
    private String title;
    
    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;
    
    @Column(name = "published_at", nullable = false)
    private LocalDateTime publishedAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @Builder
    public InfoJpaEntity(
            InfoCategory category,
            String title,
            String content,
            LocalDateTime publishedAt,
            LocalDateTime updatedAt
    ) {
        this.category = category;
        this.title = title;
        this.content = content;
        this.publishedAt = publishedAt;
        this.updatedAt = updatedAt;
    }
}
