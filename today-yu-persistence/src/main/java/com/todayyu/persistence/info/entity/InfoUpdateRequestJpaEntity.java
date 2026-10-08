package com.todayyu.persistence.info.entity;

import com.todayyu.domain.info.vo.InfoRequestStatus;
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
@Table(name = "info_update_request")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InfoUpdateRequestJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "requester_id", nullable = false)
    private Long requesterId;
    
    @Column(name = "status", nullable = false)
    @Enumerated(value = EnumType.STRING)
    private InfoRequestStatus status;
    
    @Column(name = "info_id", nullable = false)
    private Long infoId;
    
    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;
    
    @Column(name = "contact_email", nullable = false)
    private String contactEmail;
    
    @Column(name = "rejection_reason")
    private String rejectionReason;
    
    @Column(name = "observed_at", nullable = false)
    private LocalDateTime observedAt;
    
    @Column(name = "requested_at", nullable = false)
    private LocalDateTime requestedAt;
    
    @Builder
    public InfoUpdateRequestJpaEntity(
            Long requesterId,
            InfoRequestStatus status,
            Long infoId,
            String content,
            String contactEmail,
            String rejectionReason,
            LocalDateTime observedAt,
            LocalDateTime requestedAt
    ) {
        this.requesterId = requesterId;
        this.status = status;
        this.infoId = infoId;
        this.content = content;
        this.contactEmail = contactEmail;
        this.rejectionReason = rejectionReason;
        this.observedAt = observedAt;
        this.requestedAt = requestedAt;
    }
}
