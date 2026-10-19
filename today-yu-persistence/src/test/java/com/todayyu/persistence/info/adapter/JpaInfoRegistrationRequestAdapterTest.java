package com.todayyu.persistence.info.adapter;

import com.todayyu.application.info.dto.InfoRegistrationRequestCursor;
import com.todayyu.application.info.port.dto.InfoRegistrationRequestSortType;
import com.todayyu.application.info.port.dto.InfoRegistrationRequestSummaryView;
import com.todayyu.application.info.port.dto.InfoRegistrationRequestView;
import com.todayyu.domain.common.vo.Email;
import com.todayyu.domain.info.entity.InfoRegistrationRequest;
import com.todayyu.domain.info.vo.InfoCategory;
import com.todayyu.domain.info.vo.InfoId;
import com.todayyu.domain.info.vo.InfoRegistrationRequestContent;
import com.todayyu.domain.info.vo.InfoRegistrationRequestId;
import com.todayyu.domain.info.vo.InfoRegistrationRequestTitle;
import com.todayyu.domain.info.vo.InfoRequestStatus;
import com.todayyu.domain.member.vo.MemberId;
import com.todayyu.domain.member.vo.Nickname;
import com.todayyu.persistence.info.dto.InfoRegistrationRequestProjection;
import com.todayyu.persistence.info.dto.InfoRegistrationRequestSummaryViewProjection;
import com.todayyu.persistence.info.entity.InfoRegistrationRequestJpaEntity;
import com.todayyu.persistence.info.mapper.InfoRegistrationRequestMapper;
import com.todayyu.persistence.info.repository.InfoRegistrationRequestJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class JpaInfoRegistrationRequestAdapterTest {
    private static final Long INFO_ID = 1L;
    private static final Long FIRST_REQUEST_ID = 10L;
    private static final Long SECOND_REQUEST_ID = 11L;
    private static final Long FIRST_REQUESTER_ID = 100L;
    private static final Long SECOND_REQUESTER_ID = 101L;
    private static final String FIRST_REQUESTER_NICKNAME = "용감한 영대인 1";
    private static final String SECOND_REQUESTER_NICKNAME = "친절한 영대인 2";
    private static final LocalDateTime REQUESTED_AT = LocalDateTime.of(2026, 10, 1, 9, 0);

    @Mock
    private InfoRegistrationRequestJpaRepository infoRegistrationRequestRepository;

    @Mock
    private InfoRegistrationRequestMapper infoRegistrationRequestMapper;

    @InjectMocks
    private JpaInfoRegistrationRequestAdapter infoRegistrationRequestAdapter;

    @Test
    @DisplayName("정보 ID와 요청 상태에 해당하는 정보 등록 요청을 요청자 닉네임과 함께 View로 변환해 반환한다")
    void readByInfoIdAndStatus() {
        // given
        InfoRegistrationRequestJpaEntity firstJpaEntity = jpaEntity(FIRST_REQUEST_ID, FIRST_REQUESTER_ID);
        InfoRegistrationRequestJpaEntity secondJpaEntity = jpaEntity(SECOND_REQUEST_ID, SECOND_REQUESTER_ID);
        InfoRegistrationRequest firstRequest = request(FIRST_REQUEST_ID, FIRST_REQUESTER_ID);
        InfoRegistrationRequest secondRequest = request(SECOND_REQUEST_ID, SECOND_REQUESTER_ID);

        given(infoRegistrationRequestRepository.findWithRequesterNicknameByInfoIdAndStatus(INFO_ID, InfoRequestStatus.ACCEPTED))
                .willReturn(List.of(
                        new InfoRegistrationRequestProjection(firstJpaEntity, FIRST_REQUESTER_NICKNAME),
                        new InfoRegistrationRequestProjection(secondJpaEntity, SECOND_REQUESTER_NICKNAME)
                ));
        given(infoRegistrationRequestMapper.toDomain(firstJpaEntity)).willReturn(firstRequest);
        given(infoRegistrationRequestMapper.toDomain(secondJpaEntity)).willReturn(secondRequest);

        // when
        List<InfoRegistrationRequestView> result = infoRegistrationRequestAdapter.readByInfoIdAndStatus(
                new InfoId(INFO_ID),
                InfoRequestStatus.ACCEPTED
        );

        // then
        assertEquals(
                List.of(
                        new InfoRegistrationRequestView(firstRequest, new Nickname(FIRST_REQUESTER_NICKNAME)),
                        new InfoRegistrationRequestView(secondRequest, new Nickname(SECOND_REQUESTER_NICKNAME))
                ),
                result
        );
    }

    @Test
    @DisplayName("정보 ID와 요청 상태에 해당하는 정보 등록 요청이 없으면 빈 목록을 반환하고 변환하지 않는다")
    void readByInfoIdAndStatusWithoutRequests() {
        // given
        given(infoRegistrationRequestRepository.findWithRequesterNicknameByInfoIdAndStatus(INFO_ID, InfoRequestStatus.ACCEPTED))
                .willReturn(List.of());

        // when
        List<InfoRegistrationRequestView> result = infoRegistrationRequestAdapter.readByInfoIdAndStatus(
                new InfoId(INFO_ID),
                InfoRequestStatus.ACCEPTED
        );

        // then
        assertTrue(result.isEmpty());
        then(infoRegistrationRequestMapper).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("커서가 있으면 요청자 ID, 커서의 요청 일시와 정보 등록 요청 ID로 조회하고, 조회 결과를 정보 등록 요청 요약 목록으로 변환해 반환한다")
    void readByCursor() {
        // given
        LocalDateTime cursorRequestedAt = LocalDateTime.of(2026, 10, 10, 9, 0);
        InfoRegistrationRequestCursor cursor = new InfoRegistrationRequestCursor(
                cursorRequestedAt,
                new InfoRegistrationRequestId(SECOND_REQUEST_ID)
        );
        given(infoRegistrationRequestRepository.findByCursor(
                FIRST_REQUESTER_ID,
                InfoCategory.ACADEMIC,
                InfoRegistrationRequestSortType.LATEST,
                cursorRequestedAt,
                SECOND_REQUEST_ID,
                3
        )).willReturn(List.of(new InfoRegistrationRequestSummaryViewProjection(
                FIRST_REQUEST_ID,
                InfoCategory.ACADEMIC,
                "수강신청 일정 등록 요청",
                REQUESTED_AT,
                InfoRequestStatus.PENDING
        )));

        // when
        List<InfoRegistrationRequestSummaryView> result = infoRegistrationRequestAdapter.readByCursor(
                new MemberId(FIRST_REQUESTER_ID),
                InfoCategory.ACADEMIC,
                InfoRegistrationRequestSortType.LATEST,
                cursor,
                3
        );

        // then
        InfoRegistrationRequestSummaryView expected = InfoRegistrationRequestSummaryView.builder()
                .id(new InfoRegistrationRequestId(FIRST_REQUEST_ID))
                .category(InfoCategory.ACADEMIC)
                .title(new InfoRegistrationRequestTitle("수강신청 일정 등록 요청"))
                .status(InfoRequestStatus.PENDING)
                .requestedAt(REQUESTED_AT)
                .build();
        assertEquals(List.of(expected), result);
    }

    @Test
    @DisplayName("커서가 없으면 커서 조건 없이 처음부터 조회한다")
    void readByCursorWithoutCursor() {
        // given
        given(infoRegistrationRequestRepository.findByCursor(
                FIRST_REQUESTER_ID, null, InfoRegistrationRequestSortType.OLDEST, null, null, 3
        )).willReturn(List.of());

        // when
        List<InfoRegistrationRequestSummaryView> result = infoRegistrationRequestAdapter.readByCursor(
                new MemberId(FIRST_REQUESTER_ID),
                null,
                InfoRegistrationRequestSortType.OLDEST,
                null,
                3
        );

        // then
        assertTrue(result.isEmpty());
        then(infoRegistrationRequestRepository).should().findByCursor(
                FIRST_REQUESTER_ID, null, InfoRegistrationRequestSortType.OLDEST, null, null, 3
        );
    }

    @Test
    @DisplayName("정보 등록 요청을 JPA 엔티티로 변환해 저장하고, 저장된 JPA 엔티티를 도메인 엔티티로 변환해 반환한다")
    void save() {
        // given
        InfoRegistrationRequest newRequest = InfoRegistrationRequest.withoutId(
                new MemberId(FIRST_REQUESTER_ID),
                InfoCategory.ACADEMIC,
                new InfoRegistrationRequestTitle("수강신청 일정 등록 요청"),
                new InfoRegistrationRequestContent("수강신청 일정을 등록해주세요."),
                new Email("requester@yu.ac.kr"),
                REQUESTED_AT
        );
        InfoRegistrationRequestJpaEntity newJpaEntity = pendingJpaEntity(null);
        InfoRegistrationRequestJpaEntity savedJpaEntity = pendingJpaEntity(FIRST_REQUEST_ID);
        InfoRegistrationRequest savedRequest = pendingRequest(FIRST_REQUEST_ID);

        given(infoRegistrationRequestMapper.toJpaEntity(newRequest)).willReturn(newJpaEntity);
        given(infoRegistrationRequestRepository.save(newJpaEntity)).willReturn(savedJpaEntity);
        given(infoRegistrationRequestMapper.toDomain(savedJpaEntity)).willReturn(savedRequest);

        // when
        InfoRegistrationRequest result = infoRegistrationRequestAdapter.save(newRequest);

        // then
        assertSame(savedRequest, result);
        assertEquals(new InfoRegistrationRequestId(FIRST_REQUEST_ID), result.getId());
        then(infoRegistrationRequestRepository).should().save(newJpaEntity);
    }

    private InfoRegistrationRequestJpaEntity pendingJpaEntity(Long requestId) {
        InfoRegistrationRequestJpaEntity jpaEntity = InfoRegistrationRequestJpaEntity.builder()
                .requesterId(FIRST_REQUESTER_ID)
                .category(InfoCategory.ACADEMIC)
                .title("수강신청 일정 등록 요청")
                .content("수강신청 일정을 등록해주세요.")
                .status(InfoRequestStatus.PENDING)
                .contactEmail("requester@yu.ac.kr")
                .requestedAt(REQUESTED_AT)
                .build();
        ReflectionTestUtils.setField(jpaEntity, "id", requestId);
        return jpaEntity;
    }

    private InfoRegistrationRequest pendingRequest(Long requestId) {
        return InfoRegistrationRequest.withId(
                new InfoRegistrationRequestId(requestId),
                new MemberId(FIRST_REQUESTER_ID),
                InfoCategory.ACADEMIC,
                new InfoRegistrationRequestTitle("수강신청 일정 등록 요청"),
                new InfoRegistrationRequestContent("수강신청 일정을 등록해주세요."),
                InfoRequestStatus.PENDING,
                null,
                new Email("requester@yu.ac.kr"),
                null,
                REQUESTED_AT
        );
    }

    private InfoRegistrationRequestJpaEntity jpaEntity(Long requestId, Long requesterId) {
        InfoRegistrationRequestJpaEntity jpaEntity = InfoRegistrationRequestJpaEntity.builder()
                .requesterId(requesterId)
                .category(InfoCategory.ACADEMIC)
                .title("수강신청 일정 등록 요청")
                .content("수강신청 일정을 등록해주세요.")
                .status(InfoRequestStatus.ACCEPTED)
                .infoId(INFO_ID)
                .contactEmail("requester@yu.ac.kr")
                .requestedAt(REQUESTED_AT)
                .build();
        ReflectionTestUtils.setField(jpaEntity, "id", requestId);
        return jpaEntity;
    }

    private InfoRegistrationRequest request(Long requestId, Long requesterId) {
        return InfoRegistrationRequest.withId(
                new InfoRegistrationRequestId(requestId),
                new MemberId(requesterId),
                InfoCategory.ACADEMIC,
                new InfoRegistrationRequestTitle("수강신청 일정 등록 요청"),
                new InfoRegistrationRequestContent("수강신청 일정을 등록해주세요."),
                InfoRequestStatus.ACCEPTED,
                new InfoId(INFO_ID),
                new Email("requester@yu.ac.kr"),
                null,
                REQUESTED_AT
        );
    }
}
