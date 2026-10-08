package com.todayyu.persistence.info.mapper;

import com.todayyu.domain.common.vo.Email;
import com.todayyu.domain.info.entity.InfoRegistrationRequest;
import com.todayyu.domain.info.vo.InfoCategory;
import com.todayyu.domain.info.vo.InfoId;
import com.todayyu.domain.info.vo.InfoRegistrationRequestContent;
import com.todayyu.domain.info.vo.InfoRegistrationRequestId;
import com.todayyu.domain.info.vo.InfoRequestStatus;
import com.todayyu.domain.info.vo.RejectionReason;
import com.todayyu.domain.member.vo.MemberId;
import com.todayyu.persistence.info.entity.InfoRegistrationRequestJpaEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class InfoRegistrationRequestMapperTest {
    private static final Long REQUEST_ID = 1L;
    private static final Long REQUESTER_ID = 10L;
    private static final Long INFO_ID = 100L;
    private static final String CONTENT = "중앙도서관 주말 운영 시간 정보를 등록해주세요.";
    private static final String CONTACT_EMAIL = "requester@example.com";
    private static final String REJECTION_REASON = "확인되지 않은 정보입니다.";
    private static final LocalDateTime REQUESTED_AT = LocalDateTime.of(2026, 10, 1, 9, 0);

    private final InfoRegistrationRequestMapper mapper = new InfoRegistrationRequestMapper();

    @Test
    @DisplayName("신규 정보 등록 요청을 아이디가 없는 정보 등록 요청 JPA 엔티티로 변환한다")
    void toJpaEntity_pending() {
        // given
        InfoRegistrationRequest request = InfoRegistrationRequest.withoutId(
                new MemberId(REQUESTER_ID),
                InfoCategory.FACILITY,
                new InfoRegistrationRequestContent(CONTENT),
                new Email(CONTACT_EMAIL),
                REQUESTED_AT
        );

        // when
        InfoRegistrationRequestJpaEntity jpaEntity = mapper.toJpaEntity(request);

        // then
        assertNull(jpaEntity.getId());
        assertEquals(REQUESTER_ID, jpaEntity.getRequesterId());
        assertEquals(InfoCategory.FACILITY, jpaEntity.getCategory());
        assertEquals(CONTENT, jpaEntity.getContent());
        assertEquals(InfoRequestStatus.PENDING, jpaEntity.getStatus());
        assertNull(jpaEntity.getInfoId());
        assertEquals(CONTACT_EMAIL, jpaEntity.getContactEmail());
        assertNull(jpaEntity.getRejectionReason());
        assertEquals(REQUESTED_AT, jpaEntity.getRequestedAt());
    }

    @Test
    @DisplayName("승인된 정보 등록 요청을 관련 정보 아이디를 가진 JPA 엔티티로 변환한다")
    void toJpaEntity_accepted() {
        // given
        InfoRegistrationRequest request = InfoRegistrationRequest.withId(
                new InfoRegistrationRequestId(REQUEST_ID),
                new MemberId(REQUESTER_ID),
                InfoCategory.FACILITY,
                new InfoRegistrationRequestContent(CONTENT),
                InfoRequestStatus.ACCEPTED,
                new InfoId(INFO_ID),
                new Email(CONTACT_EMAIL),
                null,
                REQUESTED_AT
        );

        // when
        InfoRegistrationRequestJpaEntity jpaEntity = mapper.toJpaEntity(request);

        // then
        assertEquals(InfoRequestStatus.ACCEPTED, jpaEntity.getStatus());
        assertEquals(INFO_ID, jpaEntity.getInfoId());
        assertNull(jpaEntity.getRejectionReason());
    }

    @Test
    @DisplayName("반려된 정보 등록 요청을 반려 사유를 가진 JPA 엔티티로 변환한다")
    void toJpaEntity_rejected() {
        // given
        InfoRegistrationRequest request = InfoRegistrationRequest.withId(
                new InfoRegistrationRequestId(REQUEST_ID),
                new MemberId(REQUESTER_ID),
                InfoCategory.FACILITY,
                new InfoRegistrationRequestContent(CONTENT),
                InfoRequestStatus.REJECTED,
                null,
                new Email(CONTACT_EMAIL),
                new RejectionReason(REJECTION_REASON),
                REQUESTED_AT
        );

        // when
        InfoRegistrationRequestJpaEntity jpaEntity = mapper.toJpaEntity(request);

        // then
        assertEquals(InfoRequestStatus.REJECTED, jpaEntity.getStatus());
        assertNull(jpaEntity.getInfoId());
        assertEquals(REJECTION_REASON, jpaEntity.getRejectionReason());
    }

    @Test
    @DisplayName("대기 중인 정보 등록 요청 JPA 엔티티를 아이디를 가진 정보 등록 요청으로 변환한다")
    void toDomain_pending() {
        // given
        InfoRegistrationRequestJpaEntity jpaEntity = createJpaEntity(InfoRequestStatus.PENDING, null, null);

        // when
        InfoRegistrationRequest request = mapper.toDomain(jpaEntity);

        // then
        assertEquals(new InfoRegistrationRequestId(REQUEST_ID), request.getId());
        assertEquals(new MemberId(REQUESTER_ID), request.getRequesterId());
        assertEquals(InfoCategory.FACILITY, request.getCategory());
        assertEquals(new InfoRegistrationRequestContent(CONTENT), request.getContent());
        assertEquals(InfoRequestStatus.PENDING, request.getStatus());
        assertNull(request.getInfoId());
        assertEquals(new Email(CONTACT_EMAIL), request.getContactEmail());
        assertNull(request.getRejectionReason());
        assertEquals(REQUESTED_AT, request.getRequestedAt());
    }

    @Test
    @DisplayName("승인된 정보 등록 요청 JPA 엔티티를 관련 정보 아이디를 가진 정보 등록 요청으로 변환한다")
    void toDomain_accepted() {
        // given
        InfoRegistrationRequestJpaEntity jpaEntity = createJpaEntity(InfoRequestStatus.ACCEPTED, INFO_ID, null);

        // when
        InfoRegistrationRequest request = mapper.toDomain(jpaEntity);

        // then
        assertEquals(InfoRequestStatus.ACCEPTED, request.getStatus());
        assertEquals(new InfoId(INFO_ID), request.getInfoId());
        assertNull(request.getRejectionReason());
    }

    @Test
    @DisplayName("반려된 정보 등록 요청 JPA 엔티티를 반려 사유를 가진 정보 등록 요청으로 변환한다")
    void toDomain_rejected() {
        // given
        InfoRegistrationRequestJpaEntity jpaEntity = createJpaEntity(InfoRequestStatus.REJECTED, null, REJECTION_REASON);

        // when
        InfoRegistrationRequest request = mapper.toDomain(jpaEntity);

        // then
        assertEquals(InfoRequestStatus.REJECTED, request.getStatus());
        assertNull(request.getInfoId());
        assertEquals(new RejectionReason(REJECTION_REASON), request.getRejectionReason());
    }

    private InfoRegistrationRequestJpaEntity createJpaEntity(
            InfoRequestStatus status,
            Long infoId,
            String rejectionReason
    ) {
        InfoRegistrationRequestJpaEntity jpaEntity = InfoRegistrationRequestJpaEntity.builder()
                .requesterId(REQUESTER_ID)
                .category(InfoCategory.FACILITY)
                .content(CONTENT)
                .status(status)
                .infoId(infoId)
                .contactEmail(CONTACT_EMAIL)
                .rejectionReason(rejectionReason)
                .requestedAt(REQUESTED_AT)
                .build();
        ReflectionTestUtils.setField(jpaEntity, "id", REQUEST_ID);
        return jpaEntity;
    }
}
