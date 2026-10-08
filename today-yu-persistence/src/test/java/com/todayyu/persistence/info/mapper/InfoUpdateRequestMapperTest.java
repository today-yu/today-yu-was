package com.todayyu.persistence.info.mapper;

import com.todayyu.domain.common.vo.Email;
import com.todayyu.domain.info.entity.InfoUpdateRequest;
import com.todayyu.domain.info.vo.InfoId;
import com.todayyu.domain.info.vo.InfoRequestStatus;
import com.todayyu.domain.info.vo.InfoUpdateRequestContent;
import com.todayyu.domain.info.vo.InfoUpdateRequestId;
import com.todayyu.domain.info.vo.RejectionReason;
import com.todayyu.domain.member.vo.MemberId;
import com.todayyu.persistence.info.entity.InfoUpdateRequestJpaEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class InfoUpdateRequestMapperTest {
    private static final Long REQUEST_ID = 1L;
    private static final Long REQUESTER_ID = 10L;
    private static final Long INFO_ID = 100L;
    private static final String CONTENT = "중앙도서관 주말 운영 시간이 오후 6시까지로 변경되었습니다.";
    private static final String CONTACT_EMAIL = "requester@example.com";
    private static final String REJECTION_REASON = "확인되지 않은 정보입니다.";
    private static final LocalDateTime OBSERVED_AT = LocalDateTime.of(2026, 9, 30, 18, 0);
    private static final LocalDateTime REQUESTED_AT = LocalDateTime.of(2026, 10, 1, 9, 0);

    private final InfoUpdateRequestMapper mapper = new InfoUpdateRequestMapper();

    @Test
    @DisplayName("신규 정보 수정 요청을 아이디가 없는 정보 수정 요청 JPA 엔티티로 변환한다")
    void toJpaEntity_pending() {
        // given
        InfoUpdateRequest request = InfoUpdateRequest.withoutId(
                new MemberId(REQUESTER_ID),
                new InfoId(INFO_ID),
                new InfoUpdateRequestContent(CONTENT),
                new Email(CONTACT_EMAIL),
                OBSERVED_AT,
                REQUESTED_AT
        );

        // when
        InfoUpdateRequestJpaEntity jpaEntity = mapper.toJpaEntity(request);

        // then
        assertNull(jpaEntity.getId());
        assertEquals(REQUESTER_ID, jpaEntity.getRequesterId());
        assertEquals(InfoRequestStatus.PENDING, jpaEntity.getStatus());
        assertEquals(INFO_ID, jpaEntity.getInfoId());
        assertEquals(CONTENT, jpaEntity.getContent());
        assertEquals(CONTACT_EMAIL, jpaEntity.getContactEmail());
        assertNull(jpaEntity.getRejectionReason());
        assertEquals(OBSERVED_AT, jpaEntity.getObservedAt());
        assertEquals(REQUESTED_AT, jpaEntity.getRequestedAt());
    }

    @Test
    @DisplayName("승인된 정보 수정 요청을 반려 사유가 없는 JPA 엔티티로 변환한다")
    void toJpaEntity_accepted() {
        // given
        InfoUpdateRequest request = createDomain(InfoRequestStatus.ACCEPTED, null);

        // when
        InfoUpdateRequestJpaEntity jpaEntity = mapper.toJpaEntity(request);

        // then
        assertEquals(InfoRequestStatus.ACCEPTED, jpaEntity.getStatus());
        assertNull(jpaEntity.getRejectionReason());
    }

    @Test
    @DisplayName("반려된 정보 수정 요청을 반려 사유를 가진 JPA 엔티티로 변환한다")
    void toJpaEntity_rejected() {
        // given
        InfoUpdateRequest request = createDomain(InfoRequestStatus.REJECTED, new RejectionReason(REJECTION_REASON));

        // when
        InfoUpdateRequestJpaEntity jpaEntity = mapper.toJpaEntity(request);

        // then
        assertEquals(InfoRequestStatus.REJECTED, jpaEntity.getStatus());
        assertEquals(REJECTION_REASON, jpaEntity.getRejectionReason());
    }

    @Test
    @DisplayName("대기 중인 정보 수정 요청 JPA 엔티티를 아이디를 가진 정보 수정 요청으로 변환한다")
    void toDomain_pending() {
        // given
        InfoUpdateRequestJpaEntity jpaEntity = createJpaEntity(InfoRequestStatus.PENDING, null);

        // when
        InfoUpdateRequest request = mapper.toDomain(jpaEntity);

        // then
        assertEquals(new InfoUpdateRequestId(REQUEST_ID), request.getId());
        assertEquals(new MemberId(REQUESTER_ID), request.getRequesterId());
        assertEquals(InfoRequestStatus.PENDING, request.getStatus());
        assertEquals(new InfoId(INFO_ID), request.getInfoId());
        assertEquals(new InfoUpdateRequestContent(CONTENT), request.getContent());
        assertEquals(new Email(CONTACT_EMAIL), request.getContactEmail());
        assertNull(request.getRejectionReason());
        assertEquals(OBSERVED_AT, request.getObservedAt());
        assertEquals(REQUESTED_AT, request.getRequestedAt());
    }

    @Test
    @DisplayName("승인된 정보 수정 요청 JPA 엔티티를 반려 사유가 없는 정보 수정 요청으로 변환한다")
    void toDomain_accepted() {
        // given
        InfoUpdateRequestJpaEntity jpaEntity = createJpaEntity(InfoRequestStatus.ACCEPTED, null);

        // when
        InfoUpdateRequest request = mapper.toDomain(jpaEntity);

        // then
        assertEquals(InfoRequestStatus.ACCEPTED, request.getStatus());
        assertNull(request.getRejectionReason());
    }

    @Test
    @DisplayName("반려된 정보 수정 요청 JPA 엔티티를 반려 사유를 가진 정보 수정 요청으로 변환한다")
    void toDomain_rejected() {
        // given
        InfoUpdateRequestJpaEntity jpaEntity = createJpaEntity(InfoRequestStatus.REJECTED, REJECTION_REASON);

        // when
        InfoUpdateRequest request = mapper.toDomain(jpaEntity);

        // then
        assertEquals(InfoRequestStatus.REJECTED, request.getStatus());
        assertEquals(new RejectionReason(REJECTION_REASON), request.getRejectionReason());
    }

    private InfoUpdateRequest createDomain(InfoRequestStatus status, RejectionReason rejectionReason) {
        return InfoUpdateRequest.withId(
                new InfoUpdateRequestId(REQUEST_ID),
                new MemberId(REQUESTER_ID),
                status,
                new InfoId(INFO_ID),
                new InfoUpdateRequestContent(CONTENT),
                new Email(CONTACT_EMAIL),
                rejectionReason,
                OBSERVED_AT,
                REQUESTED_AT
        );
    }

    private InfoUpdateRequestJpaEntity createJpaEntity(InfoRequestStatus status, String rejectionReason) {
        InfoUpdateRequestJpaEntity jpaEntity = InfoUpdateRequestJpaEntity.builder()
                .requesterId(REQUESTER_ID)
                .status(status)
                .infoId(INFO_ID)
                .content(CONTENT)
                .contactEmail(CONTACT_EMAIL)
                .rejectionReason(rejectionReason)
                .observedAt(OBSERVED_AT)
                .requestedAt(REQUESTED_AT)
                .build();
        ReflectionTestUtils.setField(jpaEntity, "id", REQUEST_ID);
        return jpaEntity;
    }
}
