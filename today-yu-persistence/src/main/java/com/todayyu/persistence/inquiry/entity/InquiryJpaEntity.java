package com.todayyu.persistence.inquiry.entity;

import com.todayyu.domain.inquiry.vo.InquiryStatus;
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
@Table(name = "inquiry")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InquiryJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "member_id", nullable = false)
    private Long memberId;
    
    @Column(name = "contact_email", nullable = false)
    private String contactEmail;
    
    @Column(name = "status", nullable = false)
    @Enumerated(value = EnumType.STRING)
    private InquiryStatus status;
    
    @Column(name = "title", nullable = false)
    private String title;
    
    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @Column(name = "answered_at")
    private LocalDateTime answeredAt;
    
    @Builder
    public InquiryJpaEntity(
            Long memberId,
            String contactEmail,
            InquiryStatus status,
            String title,
            String content,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            LocalDateTime answeredAt
    ) {
        this.memberId = memberId;
        this.contactEmail = contactEmail;
        this.status = status;
        this.title = title;
        this.content = content;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.answeredAt = answeredAt;
    }
    
    /**
     * 문의의 수정 가능한 필드를 변경하는 메서드
     *
     * @param contactEmail 수정할 연락용 이메일
     * @param title        수정할 문의 제목
     * @param content      수정할 문의 내용
     * @param updatedAt    문의 수정 시각
     */
    public void update(String contactEmail, String title, String content, LocalDateTime updatedAt) {
        this.contactEmail = contactEmail;
        this.title = title;
        this.content = content;
        this.updatedAt = updatedAt;
    }
}
