package com.todayyu.domain.inquiry.entity;

import com.todayyu.domain.common.vo.Email;
import com.todayyu.domain.inquiry.vo.InquiryContent;
import com.todayyu.domain.inquiry.vo.InquiryId;
import com.todayyu.domain.inquiry.vo.InquiryStatus;
import com.todayyu.domain.inquiry.vo.InquiryTitle;
import com.todayyu.domain.member.vo.MemberId;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Objects;

@Getter
public class Inquiry {
    private InquiryId id;
    private MemberId memberId;
    private Email contactEmail;
    private InquiryStatus status;
    private InquiryTitle title;
    private InquiryContent content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime answeredAt;
    
    private Inquiry(
            InquiryId id,
            MemberId memberId,
            Email contactEmail,
            InquiryStatus status,
            InquiryTitle title,
            InquiryContent content,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            LocalDateTime answeredAt
    ) {
        this.id = id;
        this.memberId = memberId;
        this.contactEmail = contactEmail;
        this.status = status;
        this.title = title;
        this.content = content;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.answeredAt = answeredAt;
    }
    
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Inquiry inquiry)) return false;
        return Objects.equals(id, inquiry.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
    
    public static Inquiry withoutId(
            MemberId memberId,
            Email contactEmail,
            InquiryTitle title,
            InquiryContent content,
            LocalDateTime createdAt
    ) {
        validateRequiredFields(memberId, contactEmail, title, content, createdAt, createdAt);
        
        return new Inquiry(
                null,
                memberId,
                contactEmail,
                InquiryStatus.WAITING,
                title,
                content,
                createdAt,
                createdAt,
                null
        );
    }
    
    public static Inquiry withId(
            InquiryId id,
            MemberId memberId,
            Email contactEmail,
            InquiryStatus status,
            InquiryTitle title,
            InquiryContent content,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            LocalDateTime answeredAt
    ) {
        if (id == null) {
            throw new IllegalArgumentException("문의 ID는 비어있을 수 없습니다.");
        }
        
        if (status == null) {
            throw new IllegalArgumentException("문의 상태는 비어있을 수 없습니다.");
        }
        
        validateRequiredFields(memberId, contactEmail, title, content, createdAt, updatedAt);
        validateStatus(status, answeredAt);

        return new Inquiry(id, memberId, contactEmail, status, title, content, createdAt, updatedAt, answeredAt);
    }
    
    /**
     * 필수 입력 필드에 대한 검사 정적 메서드
     *
     * @param memberId     문의 작성 회원 ID
     * @param contactEmail 문의 작성자 연락 이메일
     * @param title        문의 제목
     * @param content      문의 내용
     * @param createdAt    문의 작성 시각
     * @param updatedAt    문의 수정 시각
     * @throws IllegalArgumentException 필수 입력 필드가 누락된 경우
     * <ul>
     *     <li> {@code memberId}가 비어있는 경우 </li>
     *     <li> {@code contactEmail}가 비어있는 경우 </li>
     *     <li> {@code title}가 비어있는 경우 </li>
     *     <li> {@code content}가 비어있는 경우 </li>
     *     <li> {@code createdAt}가 비어있는 경우 </li>
     *     <li> {@code updatedAt}가 비어있는 경우 </li>
     * </ul>
     */
    private static void validateRequiredFields(
            MemberId memberId,
            Email contactEmail,
            InquiryTitle title,
            InquiryContent content,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        if (memberId == null) {
            throw new IllegalArgumentException("문의 작성자(회원) ID는 비어있을 수 없습니다.");
        }
        
        if (contactEmail == null) {
            throw new IllegalArgumentException("연락용 이메일은 비어있을 수 없습니다.");
        }
        
        if (title == null) {
            throw new IllegalArgumentException("문의 제목은 비어있을 수 없습니다.");
        }
        
        if (content == null) {
            throw new IllegalArgumentException("문의 내용은 비어있을 수 없습니다.");
        }
        
        if (createdAt == null) {
            throw new IllegalArgumentException("문의 작성 시각은 비어있을 수 없습니다.");
        }
        
        if (updatedAt == null) {
            throw new IllegalArgumentException("문의 수정 시각은 비어있을 수 없습니다.");
        }
    }
    
    /**
     * 문의 상태에 따른 답변 시각({@link #answeredAt})이 유효한지 확인하는 정적 메서드
     *
     * <ul>
     *     <li> {@link InquiryStatus#WAITING} - 답변 시각이 비어있다. </li>
     *     <li> {@link InquiryStatus#ANSWERED} - 답변 시각이 존재한다. </li>
     * </ul>
     *
     * @param status     문의 상태
     * @param answeredAt 답변 시각
     * @throws IllegalArgumentException 문의 상태와 답변 시각 상태가 불일치하는 경우
     */
    private static void validateStatus(InquiryStatus status, LocalDateTime answeredAt) {
        boolean valid = switch (status) {
            case WAITING -> answeredAt == null;
            case ANSWERED -> answeredAt != null;
        };
        
        if (!valid) {
            throw new IllegalArgumentException("문의 상태와 답변 시각 상태가 일치하지 않습니다.");
        }
    }
}
