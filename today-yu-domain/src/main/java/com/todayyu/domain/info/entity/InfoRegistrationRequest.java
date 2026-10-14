package com.todayyu.domain.info.entity;

import com.todayyu.domain.common.vo.Email;
import com.todayyu.domain.info.vo.InfoCategory;
import com.todayyu.domain.info.vo.InfoId;
import com.todayyu.domain.info.vo.InfoRegistrationRequestContent;
import com.todayyu.domain.info.vo.InfoRegistrationRequestId;
import com.todayyu.domain.info.vo.InfoRegistrationRequestTitle;
import com.todayyu.domain.info.vo.InfoRequestStatus;
import com.todayyu.domain.info.vo.RejectionReason;
import com.todayyu.domain.member.vo.MemberId;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Objects;

@Getter
public class InfoRegistrationRequest {
    private InfoRegistrationRequestId id;
    private MemberId requesterId;
    private InfoCategory category;
    private InfoRegistrationRequestTitle title;
    private InfoRegistrationRequestContent content;
    private InfoRequestStatus status;
    private InfoId infoId;
    private Email contactEmail;
    private RejectionReason rejectionReason;
    private LocalDateTime requestedAt;
    
    private InfoRegistrationRequest(
            InfoRegistrationRequestId id,
            MemberId requesterId,
            InfoCategory category,
            InfoRegistrationRequestTitle title,
            InfoRegistrationRequestContent content,
            InfoRequestStatus status,
            InfoId infoId,
            Email contactEmail,
            RejectionReason rejectionReason,
            LocalDateTime requestedAt
    ) {
        this.id = id;
        this.requesterId = requesterId;
        this.category = category;
        this.title = title;
        this.content = content;
        this.status = status;
        this.infoId = infoId;
        this.contactEmail = contactEmail;
        this.rejectionReason = rejectionReason;
        this.requestedAt = requestedAt;
    }
    
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof InfoRegistrationRequest that)) return false;
        return Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
    
    public static InfoRegistrationRequest withoutId(
            MemberId requesterId,
            InfoCategory category,
            InfoRegistrationRequestTitle title,
            InfoRegistrationRequestContent content,
            Email contactEmail,
            LocalDateTime requestedAt
    ) {
        validateRequiredFields(requesterId, category, title, content, contactEmail);
        
        return new InfoRegistrationRequest(
                null,
                requesterId,
                category,
                title,
                content,
                InfoRequestStatus.PENDING,
                null,
                contactEmail,
                null,
                requestedAt
        );
    }
    
    public static InfoRegistrationRequest withId(
            InfoRegistrationRequestId id,
            MemberId requesterId,
            InfoCategory category,
            InfoRegistrationRequestTitle title,
            InfoRegistrationRequestContent content,
            InfoRequestStatus status,
            InfoId infoId,
            Email contactEmail,
            RejectionReason rejectionReason,
            LocalDateTime requestedAt
    ) {
        if (id == null) {
            throw new IllegalArgumentException("정보 등록 요청 ID는 비어있을 수 없습니다.");
        }
        
        validateRequiredFields(requesterId, category, title, content, contactEmail);
        validateStatus(status, infoId, rejectionReason);
        
        return new InfoRegistrationRequest(
                id,
                requesterId,
                category,
                title,
                content,
                status,
                infoId,
                contactEmail,
                rejectionReason,
                requestedAt
        );
    }
    
    /**
     * 필수 입력 필드에 대한 검사 정적 메서드
     *
     * @param requesterId  정보 등록 요청 회원 ID
     * @param category     정보 카테고리
     * @param title        정보 등록 요청 제목
     * @param content      정보 등록 요청 내용
     * @param contactEmail 정보 등록 요청자 연락 이메일
     * @throws IllegalArgumentException 필수 입력 필드가 누락된 경우
     * <ul>
     *     <li> {@code requesterId}가 비어있는 경우 </li>
     *     <li> {@code category}가 비어있는 경우 </li>
     *     <li> {@code title}가 비어있는 경우 </li>
     *     <li> {@code content}가 비어있는 경우 </li>
     *     <li> {@code contactEmail}가 비어있는 경우 </li>
     * </ul>
     */
    private static void validateRequiredFields(
            MemberId requesterId,
            InfoCategory category,
            InfoRegistrationRequestTitle title,
            InfoRegistrationRequestContent content,
            Email contactEmail
    ) {
        if (requesterId == null) {
            throw new IllegalArgumentException("정보 등록 요청 회원 ID는 비어있을 수 없습니다.");
        }
        
        if (category == null) {
            throw new IllegalArgumentException("정보 카테고리는 비어있을 수 없습니다.");
        }
        
        if (title == null) {
            throw new IllegalArgumentException("정보 등록 요청 제목은 비어있을 수 없습니다.");
        }
        
        if (content == null) {
            throw new IllegalArgumentException("정보 등록 요청 내용은 비어있을 수 없습니다.");
        }
        
        if (contactEmail == null) {
            throw new IllegalArgumentException("연락용 이메일은 비어있을 수 없습니다.");
        }
    }
    
    /**
     * 정보 등록 요청 상태에 따른 관련 정보({@link #infoId})와 반려 사유({@link #rejectionReason})이 유효한지
     * 확인하는 정적 메서드
     *
     * <ul>
     *     <li> {@link InfoRequestStatus#PENDING} - 관련 정보와 반려 사유 모두 비어있다. </li>
     *     <li> {@link InfoRequestStatus#ACCEPTED} - 관련 정보는 존재하고, 반려 사유는 비어있다. </li>
     *     <li> {@link InfoRequestStatus#REJECTED} - 관련 정보는 비어있고, 반려 사유는 존재한다. </li>
     * </ul>
     *
     * @param status          정보 등록 요청 상태
     * @param infoId          관련 정보 ID
     * @param rejectionReason 반려 사유
     * @throws IllegalArgumentException 정보 등록 요청에 따른 정보와 반려 사유 상태가 불일치하는 경우
     */
    private static void validateStatus(InfoRequestStatus status, InfoId infoId, RejectionReason rejectionReason) {
        if (status == null) {
            throw new IllegalArgumentException("정보 등록 요청 상태는 비어있을 수 없습니다.");
        }
        
        boolean valid = switch (status) {
            case PENDING -> infoId == null && rejectionReason == null;
            case ACCEPTED -> infoId != null && rejectionReason == null;
            case REJECTED -> infoId == null && rejectionReason != null;
        };
        
        if (!valid) {
            throw new IllegalArgumentException("정보 등록 요청 상태와 관련 정보, 반려 사유가 유효하지 않습니다.");
        }
    }
    
    /**
     * 주어진 회원이 정보 등록 요청을 작성한 회원인지 확인하는 메서드
     *
     * @param memberId 확인할 회원 ID
     * @return 정보 등록 요청 작성자 여부
     */
    public boolean isRequester(MemberId memberId) {
        return requesterId.equals(memberId);
    }
}
