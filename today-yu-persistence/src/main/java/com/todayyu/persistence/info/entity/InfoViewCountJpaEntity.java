package com.todayyu.persistence.info.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Persistable;

@Getter
@Entity
@Table(name = "info_view_count")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InfoViewCountJpaEntity implements Persistable<Long> {
    @Id
    @Column(name = "info_id", nullable = false)
    private Long infoId;
    
    @Column(name = "view_count", nullable = false)
    private Long viewCount;
    
    @Transient
    private boolean isNew = true;
    
    @Override
    public @Nullable Long getId() {
        return infoId;
    }
    
    @Override
    public boolean isNew() {
        return isNew;
    }
    
    @PostLoad
    @PrePersist
    void markNotNew() {
        this.isNew = false;
    }
    
    @Builder
    public InfoViewCountJpaEntity(Long infoId, Long viewCount) {
        this.infoId = infoId;
        this.viewCount = viewCount;
    }
}
