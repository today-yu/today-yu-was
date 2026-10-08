package com.todayyu.persistence.info.entity;

import com.todayyu.domain.info.vo.InfoCategory;
import com.todayyu.domain.info.vo.InfoId;
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
@Table(name = "info_registration_request")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InfoRegistrationRequestJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "requester_id", nullable = false)
    private Long requesterId;
    
    @Column(name = "category", nullable = false)
    @Enumerated(value = EnumType.STRING)
    private InfoCategory category;
    
    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;
    
    @Column(name = "status", nullable = false)
    @Enumerated(value = EnumType.STRING)
    private InfoRequestStatus status;
    
    @Column(name = "info_id")
    private Long infoId;
    
    @Column(name = "contact_email", nullable = false)
    private String contactEmail;
    
    @Column(name = "rejection_reason")
    private String rejectionReason;
    
    @Column(name = "requested_at", nullable = false)
    private LocalDateTime requestedAt;
    
    @Builder
    public InfoRegistrationRequestJpaEntity(
            Long requesterId,
            InfoCategory category,
            String content,
            InfoRequestStatus status,
            Long infoId,
            String contactEmail,
            String rejectionReason,
            LocalDateTime requestedAt
    ) {
        this.requesterId = requesterId;
        this.category = category;
        this.content = content;
        this.status = status;
        this.infoId = infoId;
        this.contactEmail = contactEmail;
        this.rejectionReason = rejectionReason;
        this.requestedAt = requestedAt;
    }
}
