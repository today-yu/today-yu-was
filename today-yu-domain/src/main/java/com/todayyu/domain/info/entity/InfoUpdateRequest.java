package com.todayyu.domain.info.entity;

import com.todayyu.domain.common.vo.Email;
import com.todayyu.domain.info.vo.InfoId;
import com.todayyu.domain.info.vo.InfoRequestStatus;
import com.todayyu.domain.info.vo.InfoUpdateRequestContent;
import com.todayyu.domain.info.vo.InfoUpdateRequestId;
import com.todayyu.domain.info.vo.RejectionReason;
import com.todayyu.domain.member.vo.MemberId;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Objects;

@Getter
public class InfoUpdateRequest {
    private InfoUpdateRequestId id;
    private MemberId requesterId;
    private InfoRequestStatus status;
    private InfoId infoId;
    private InfoUpdateRequestContent content;
    private Email contactEmail;
    private RejectionReason rejectionReason;
    private LocalDateTime observedAt;
    private LocalDateTime requestedAt;
    
    private InfoUpdateRequest(
            InfoUpdateRequestId id,
            MemberId requesterId,
            InfoRequestStatus status,
            InfoId infoId,
            InfoUpdateRequestContent content,
            Email contactEmail,
            RejectionReason rejectionReason,
            LocalDateTime observedAt,
            LocalDateTime requestedAt
    ) {
        this.id = id;
        this.requesterId = requesterId;
        this.status = status;
        this.infoId = infoId;
        this.content = content;
        this.contactEmail = contactEmail;
        this.rejectionReason = rejectionReason;
        this.observedAt = observedAt;
        this.requestedAt = requestedAt;
    }
    
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof InfoUpdateRequest that)) return false;
        return Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
    
    public static InfoUpdateRequest withoutId(
            MemberId requesterId,
            InfoId infoId,
            InfoUpdateRequestContent content,
            Email contactEmail,
            LocalDateTime observedAt,
            LocalDateTime requestedAt
    ) {
        validateRequiredFields(requesterId, infoId, content, contactEmail);
        
        return new InfoUpdateRequest(
                null,
                requesterId,
                InfoRequestStatus.PENDING,
                infoId,
                content,
                contactEmail,
                null,
                observedAt,
                requestedAt
        );
    }
    
    public static InfoUpdateRequest withId(
            InfoUpdateRequestId id,
            MemberId requesterId,
            InfoRequestStatus status,
            InfoId infoId,
            InfoUpdateRequestContent content,
            Email contactEmail,
            RejectionReason rejectionReason,
            LocalDateTime observedAt,
            LocalDateTime requestedAt
    ) {
        if (id == null) {
            throw new IllegalArgumentException("정보 수정 요청 ID는 비어있을 수 없습니다.");
        }
        
        validateRequiredFields(requesterId, infoId, content, contactEmail);
        validateStatus(status, rejectionReason);
        
        return new InfoUpdateRequest(
                id,
                requesterId,
                status,
                infoId,
                content,
                contactEmail,
                rejectionReason,
                observedAt,
                requestedAt
        );
    }

    /**
     * 필수 입력 필드에 대한 검사 정적 메서드
     *
     * @param requesterId  정보 수정 요청 회원 ID
     * @param infoId       정보 수정 요청 대상 정보 ID
     * @param content      정보 수정 요청 내용
     * @param contactEmail 정보 수정 요청자 연락 이메일
     * @throws IllegalArgumentException 필수 입력 필드가 누락된 경우
     * <ul>
     *     <li> {@code requesterId}가 비어있는 경우 </li>
     *     <li> {@code infoId}가 비어있는 경우 </li>
     *     <li> {@code content}가 비어있는 경우 </li>
     *     <li> {@code contactEmail}가 비어있는 경우 </li>
     * </ul>
     */
    private static void validateRequiredFields(
            MemberId requesterId,
            InfoId infoId,
            InfoUpdateRequestContent content,
            Email contactEmail
    ) {
        if (requesterId == null) {
            throw new IllegalArgumentException("정보 수정 요청 회원 ID는 비어있을 수 없습니다.");
        }
        
        if (infoId == null) {
            throw new IllegalArgumentException("정보 수정 요청 대상 정보 ID는 비어있을 수 없습니다.");
        }
        
        if (content == null) {
            throw new IllegalArgumentException("정보 수정 요청 내용은 비어있을 수 없습니다.");
        }
        
        if (contactEmail == null) {
            throw new IllegalArgumentException("연락용 이메일은 비어있을 수 없습니다.");
        }
    }

    /**
     * 정보 수정 요청 상태에 따른 반려 사유({@link #rejectionReason})가 유효한지 확인하는 정적 메서드
     *
     * <ul>
     *     <li> {@link InfoRequestStatus#PENDING} - 반려 사유는 비어있다. </li>
     *     <li> {@link InfoRequestStatus#ACCEPTED} - 반려 사유는 비어있다. </li>
     *     <li> {@link InfoRequestStatus#REJECTED} - 반려 사유는 존재한다. </li>
     * </ul>
     *
     * @param status          정보 수정 요청 상태
     * @param rejectionReason 반려 사유
     * @throws IllegalArgumentException 다음 중 하나에 해당하는 경우
     * <ul>
     *     <li> {@code status}가 비어있는 경우 </li>
     *     <li> 정보 수정 요청 상태와 반려 사유가 불일치하는 경우 </li>
     * </ul>
     */
    private static void validateStatus(InfoRequestStatus status, RejectionReason rejectionReason) {
        if (status == null) {
            throw new IllegalArgumentException("정보 수정 요청 상태는 비어있을 수 없습니다.");
        }
        
        boolean valid = switch (status) {
            case PENDING, ACCEPTED -> rejectionReason == null;
            case REJECTED -> rejectionReason != null;
        };
        
        if (!valid) {
            throw new IllegalArgumentException("정보 수정 요청 상태와 반려 사유가 유효하지 않습니다.");
        }
    
    }
}
