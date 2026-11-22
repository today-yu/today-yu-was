package com.todayyu.application.info.listener;

import com.todayyu.application.info.port.read.ReadInfoUpdateRequestPort;
import com.todayyu.application.info.port.save.SaveInfoUpdateRequestPort;
import com.todayyu.application.member.event.MemberWithdrawalEvent;
import com.todayyu.domain.common.vo.Email;
import com.todayyu.domain.info.entity.InfoUpdateRequest;
import com.todayyu.domain.info.vo.InfoId;
import com.todayyu.domain.info.vo.InfoRequestStatus;
import com.todayyu.domain.info.vo.InfoUpdateRequestContent;
import com.todayyu.domain.info.vo.InfoUpdateRequestId;
import com.todayyu.domain.info.vo.RejectionReason;
import com.todayyu.domain.member.vo.MemberId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class InfoUpdateRequestMemberWithdrawalListenerTest {
    private static final MemberId REQUESTER_ID = new MemberId(1L);
    private static final InfoId INFO_ID = new InfoId(10L);
    private static final InfoUpdateRequestContent CONTENT = new InfoUpdateRequestContent("정보 수정 요청 내용");
    private static final Email CONTACT_EMAIL = new Email("user@example.com");
    private static final RejectionReason REJECTION_REASON = new RejectionReason("변경 사실을 확인할 수 없습니다.");
    private static final LocalDateTime OBSERVED_AT = LocalDateTime.of(2026, 9, 30, 18, 0);
    private static final LocalDateTime REQUESTED_AT = LocalDateTime.of(2026, 10, 1, 9, 0);

    @Mock
    private ReadInfoUpdateRequestPort readInfoUpdateRequestPort;

    @Mock
    private SaveInfoUpdateRequestPort saveInfoUpdateRequestPort;

    @InjectMocks
    private InfoUpdateRequestMemberWithdrawalListener listener;

    @Captor
    private ArgumentCaptor<List<InfoUpdateRequestId>> requestIdsCaptor;

    @Captor
    private ArgumentCaptor<List<InfoUpdateRequest>> requestsCaptor;

    @Test
    @DisplayName("대기·반려 요청은 삭제하고, 반영된 요청은 연락 이메일을 제거하여 비식별화한다")
    void deleteOrAnonymizeRequests() {
        // given
        InfoUpdateRequest pending = request(1L, InfoRequestStatus.PENDING, null);
        InfoUpdateRequest rejected = request(2L, InfoRequestStatus.REJECTED, REJECTION_REASON);
        InfoUpdateRequest accepted = request(3L, InfoRequestStatus.ACCEPTED, null);
        given(readInfoUpdateRequestPort.readAllByMemberId(REQUESTER_ID))
                .willReturn(List.of(pending, rejected, accepted));

        // when
        listener.deleteOrAnonymizeRequests(withdrawalEvent());

        // then
        then(saveInfoUpdateRequestPort).should().deleteByIds(requestIdsCaptor.capture());
        assertEquals(List.of(pending.getId(), rejected.getId()), requestIdsCaptor.getValue());

        then(saveInfoUpdateRequestPort).should().anonymizeAll(requestsCaptor.capture());
        List<InfoUpdateRequest> anonymized = requestsCaptor.getValue();
        assertEquals(1, anonymized.size());
        assertEquals(accepted.getId(), anonymized.get(0).getId());
        assertNull(anonymized.get(0).getContactEmail());
    }

    @Test
    @DisplayName("비식별화된 요청은 내용, 상태, 연결된 정보가 유지된다")
    void keepAcceptedRequestContents() {
        // given
        given(readInfoUpdateRequestPort.readAllByMemberId(REQUESTER_ID))
                .willReturn(List.of(request(1L, InfoRequestStatus.ACCEPTED, null)));

        // when
        listener.deleteOrAnonymizeRequests(withdrawalEvent());

        // then
        then(saveInfoUpdateRequestPort).should().anonymizeAll(requestsCaptor.capture());
        InfoUpdateRequest anonymized = requestsCaptor.getValue().get(0);
        assertEquals(REQUESTER_ID, anonymized.getRequesterId());
        assertEquals(CONTENT, anonymized.getContent());
        assertEquals(InfoRequestStatus.ACCEPTED, anonymized.getStatus());
        assertEquals(INFO_ID, anonymized.getInfoId());
    }

    @Test
    @DisplayName("반영된 요청이 없으면 비식별화 대상은 빈 목록으로 전달된다")
    void noAcceptedRequests() {
        // given
        InfoUpdateRequest pending = request(1L, InfoRequestStatus.PENDING, null);
        given(readInfoUpdateRequestPort.readAllByMemberId(REQUESTER_ID)).willReturn(List.of(pending));

        // when
        listener.deleteOrAnonymizeRequests(withdrawalEvent());

        // then
        then(saveInfoUpdateRequestPort).should().deleteByIds(List.of(pending.getId()));
        then(saveInfoUpdateRequestPort).should().anonymizeAll(requestsCaptor.capture());
        assertTrue(requestsCaptor.getValue().isEmpty());
    }

    @Test
    @DisplayName("정보 수정 요청이 없으면 삭제·비식별화 대상은 모두 빈 목록으로 전달된다")
    void noRequests() {
        // given
        given(readInfoUpdateRequestPort.readAllByMemberId(REQUESTER_ID)).willReturn(List.of());

        // when
        listener.deleteOrAnonymizeRequests(withdrawalEvent());

        // then
        then(saveInfoUpdateRequestPort).should().deleteByIds(List.of());
        then(saveInfoUpdateRequestPort).should().anonymizeAll(List.of());
    }

    private MemberWithdrawalEvent withdrawalEvent() {
        return new MemberWithdrawalEvent(REQUESTER_ID, List.of());
    }

    private InfoUpdateRequest request(Long id, InfoRequestStatus status, RejectionReason rejectionReason) {
        return InfoUpdateRequest.withId(
                new InfoUpdateRequestId(id),
                REQUESTER_ID,
                status,
                INFO_ID,
                CONTENT,
                CONTACT_EMAIL,
                rejectionReason,
                OBSERVED_AT,
                REQUESTED_AT
        );
    }
}
