package com.todayyu.persistence.info.adapter;

import com.todayyu.application.info.dto.InfoUpdateRequestView;
import com.todayyu.domain.common.vo.Email;
import com.todayyu.domain.info.entity.InfoUpdateRequest;
import com.todayyu.domain.info.vo.InfoId;
import com.todayyu.domain.info.vo.InfoRequestStatus;
import com.todayyu.domain.info.vo.InfoUpdateRequestContent;
import com.todayyu.domain.info.vo.InfoUpdateRequestId;
import com.todayyu.domain.member.vo.MemberId;
import com.todayyu.domain.member.vo.Nickname;
import com.todayyu.persistence.info.dto.InfoUpdateRequestProjection;
import com.todayyu.persistence.info.entity.InfoUpdateRequestJpaEntity;
import com.todayyu.persistence.info.mapper.InfoUpdateRequestMapper;
import com.todayyu.persistence.info.repository.InfoUpdateRequestJpaRepository;
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
class JpaInfoUpdateRequestAdapterTest {
    private static final Long INFO_ID = 1L;
    private static final Long FIRST_REQUEST_ID = 10L;
    private static final Long SECOND_REQUEST_ID = 11L;
    private static final Long FIRST_REQUESTER_ID = 100L;
    private static final Long SECOND_REQUESTER_ID = 101L;
    private static final String FIRST_REQUESTER_NICKNAME = "용감한 영대인 1";
    private static final String SECOND_REQUESTER_NICKNAME = "친절한 영대인 2";
    private static final LocalDateTime OBSERVED_AT = LocalDateTime.of(2026, 10, 1, 9, 0);
    private static final LocalDateTime REQUESTED_AT = LocalDateTime.of(2026, 10, 2, 9, 0);

    @Mock
    private InfoUpdateRequestJpaRepository infoUpdateRequestRepository;

    @Mock
    private InfoUpdateRequestMapper infoUpdateRequestMapper;

    @InjectMocks
    private JpaInfoUpdateRequestAdapter infoUpdateRequestAdapter;

    @Test
    @DisplayName("정보 ID와 요청 상태에 해당하는 정보 수정 요청을 요청자 닉네임과 함께 View로 변환해 반환한다")
    void readByInfoIdAndStatus() {
        // given
        InfoUpdateRequestJpaEntity firstJpaEntity = jpaEntity(FIRST_REQUEST_ID, FIRST_REQUESTER_ID);
        InfoUpdateRequestJpaEntity secondJpaEntity = jpaEntity(SECOND_REQUEST_ID, SECOND_REQUESTER_ID);
        InfoUpdateRequest firstRequest = request(FIRST_REQUEST_ID, FIRST_REQUESTER_ID);
        InfoUpdateRequest secondRequest = request(SECOND_REQUEST_ID, SECOND_REQUESTER_ID);

        given(infoUpdateRequestRepository.findWithRequesterNicknameByInfoIdAndStatus(INFO_ID, InfoRequestStatus.ACCEPTED))
                .willReturn(List.of(
                        new InfoUpdateRequestProjection(firstJpaEntity, FIRST_REQUESTER_NICKNAME),
                        new InfoUpdateRequestProjection(secondJpaEntity, SECOND_REQUESTER_NICKNAME)
                ));
        given(infoUpdateRequestMapper.toDomain(firstJpaEntity)).willReturn(firstRequest);
        given(infoUpdateRequestMapper.toDomain(secondJpaEntity)).willReturn(secondRequest);

        // when
        List<InfoUpdateRequestView> result = infoUpdateRequestAdapter.readByInfoIdAndStatus(
                new InfoId(INFO_ID),
                InfoRequestStatus.ACCEPTED
        );

        // then
        assertEquals(
                List.of(
                        new InfoUpdateRequestView(firstRequest, new Nickname(FIRST_REQUESTER_NICKNAME)),
                        new InfoUpdateRequestView(secondRequest, new Nickname(SECOND_REQUESTER_NICKNAME))
                ),
                result
        );
    }

    @Test
    @DisplayName("정보 ID와 요청 상태에 해당하는 정보 수정 요청이 없으면 빈 목록을 반환하고 변환하지 않는다")
    void readByInfoIdAndStatusWithoutRequests() {
        // given
        given(infoUpdateRequestRepository.findWithRequesterNicknameByInfoIdAndStatus(INFO_ID, InfoRequestStatus.ACCEPTED))
                .willReturn(List.of());

        // when
        List<InfoUpdateRequestView> result = infoUpdateRequestAdapter.readByInfoIdAndStatus(
                new InfoId(INFO_ID),
                InfoRequestStatus.ACCEPTED
        );

        // then
        assertTrue(result.isEmpty());
        then(infoUpdateRequestMapper).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("정보 수정 요청을 JPA 엔티티로 변환해 저장하고, 저장된 JPA 엔티티를 도메인 엔티티로 변환해 반환한다")
    void save() {
        // given
        InfoUpdateRequest newRequest = InfoUpdateRequest.withoutId(
                new MemberId(FIRST_REQUESTER_ID),
                new InfoId(INFO_ID),
                new InfoUpdateRequestContent("수강신청 일정이 10월 21일로 변경되었습니다."),
                new Email("requester@yu.ac.kr"),
                OBSERVED_AT,
                REQUESTED_AT
        );
        InfoUpdateRequestJpaEntity newJpaEntity = pendingJpaEntity(null);
        InfoUpdateRequestJpaEntity savedJpaEntity = pendingJpaEntity(FIRST_REQUEST_ID);
        InfoUpdateRequest savedRequest = pendingRequest(FIRST_REQUEST_ID);

        given(infoUpdateRequestMapper.toJpaEntity(newRequest)).willReturn(newJpaEntity);
        given(infoUpdateRequestRepository.save(newJpaEntity)).willReturn(savedJpaEntity);
        given(infoUpdateRequestMapper.toDomain(savedJpaEntity)).willReturn(savedRequest);

        // when
        InfoUpdateRequest result = infoUpdateRequestAdapter.save(newRequest);

        // then
        assertSame(savedRequest, result);
        assertEquals(new InfoUpdateRequestId(FIRST_REQUEST_ID), result.getId());
        then(infoUpdateRequestRepository).should().save(newJpaEntity);
    }

    private InfoUpdateRequestJpaEntity pendingJpaEntity(Long requestId) {
        InfoUpdateRequestJpaEntity jpaEntity = InfoUpdateRequestJpaEntity.builder()
                .requesterId(FIRST_REQUESTER_ID)
                .status(InfoRequestStatus.PENDING)
                .infoId(INFO_ID)
                .content("수강신청 일정이 10월 21일로 변경되었습니다.")
                .contactEmail("requester@yu.ac.kr")
                .observedAt(OBSERVED_AT)
                .requestedAt(REQUESTED_AT)
                .build();
        ReflectionTestUtils.setField(jpaEntity, "id", requestId);
        return jpaEntity;
    }

    private InfoUpdateRequest pendingRequest(Long requestId) {
        return InfoUpdateRequest.withId(
                new InfoUpdateRequestId(requestId),
                new MemberId(FIRST_REQUESTER_ID),
                InfoRequestStatus.PENDING,
                new InfoId(INFO_ID),
                new InfoUpdateRequestContent("수강신청 일정이 10월 21일로 변경되었습니다."),
                new Email("requester@yu.ac.kr"),
                null,
                OBSERVED_AT,
                REQUESTED_AT
        );
    }

    private InfoUpdateRequestJpaEntity jpaEntity(Long requestId, Long requesterId) {
        InfoUpdateRequestJpaEntity jpaEntity = InfoUpdateRequestJpaEntity.builder()
                .requesterId(requesterId)
                .status(InfoRequestStatus.ACCEPTED)
                .infoId(INFO_ID)
                .content("수강신청 일정이 10월 21일로 변경되었습니다.")
                .contactEmail("requester@yu.ac.kr")
                .observedAt(OBSERVED_AT)
                .requestedAt(REQUESTED_AT)
                .build();
        ReflectionTestUtils.setField(jpaEntity, "id", requestId);
        return jpaEntity;
    }

    private InfoUpdateRequest request(Long requestId, Long requesterId) {
        return InfoUpdateRequest.withId(
                new InfoUpdateRequestId(requestId),
                new MemberId(requesterId),
                InfoRequestStatus.ACCEPTED,
                new InfoId(INFO_ID),
                new InfoUpdateRequestContent("수강신청 일정이 10월 21일로 변경되었습니다."),
                new Email("requester@yu.ac.kr"),
                null,
                OBSERVED_AT,
                REQUESTED_AT
        );
    }
}
