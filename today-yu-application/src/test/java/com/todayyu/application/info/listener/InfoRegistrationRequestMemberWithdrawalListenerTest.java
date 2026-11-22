package com.todayyu.application.info.listener;

import com.todayyu.application.info.port.read.ReadInfoRegistrationRequestPort;
import com.todayyu.application.info.port.save.SaveInfoRegistrationRequestPort;
import com.todayyu.application.member.event.MemberWithdrawalEvent;
import com.todayyu.domain.common.vo.Email;
import com.todayyu.domain.info.entity.InfoRegistrationRequest;
import com.todayyu.domain.info.vo.InfoCategory;
import com.todayyu.domain.info.vo.InfoId;
import com.todayyu.domain.info.vo.InfoRegistrationRequestContent;
import com.todayyu.domain.info.vo.InfoRegistrationRequestId;
import com.todayyu.domain.info.vo.InfoRegistrationRequestTitle;
import com.todayyu.domain.info.vo.InfoRequestStatus;
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
class InfoRegistrationRequestMemberWithdrawalListenerTest {
    private static final MemberId REQUESTER_ID = new MemberId(1L);
    private static final InfoCategory CATEGORY = InfoCategory.ACADEMIC;
    private static final InfoRegistrationRequestTitle TITLE = new InfoRegistrationRequestTitle("정보 등록 요청 제목");
    private static final InfoRegistrationRequestContent CONTENT = new InfoRegistrationRequestContent("정보 등록 요청 내용");
    private static final Email CONTACT_EMAIL = new Email("user@example.com");
    private static final LocalDateTime REQUESTED_AT = LocalDateTime.of(2026, 10, 1, 9, 0);
    private static final InfoId INFO_ID = new InfoId(10L);
    private static final RejectionReason REJECTION_REASON = new RejectionReason("사실 관계를 확인할 수 없습니다.");

    @Mock
    private ReadInfoRegistrationRequestPort readInfoRegistrationRequestPort;

    @Mock
    private SaveInfoRegistrationRequestPort saveInfoRegistrationRequestPort;

    @InjectMocks
    private InfoRegistrationRequestMemberWithdrawalListener listener;

    @Captor
    private ArgumentCaptor<List<InfoRegistrationRequestId>> requestIdsCaptor;

    @Captor
    private ArgumentCaptor<List<InfoRegistrationRequest>> requestsCaptor;

    @Test
    @DisplayName("대기·반려 요청은 삭제하고, 반영된 요청은 연락 이메일을 제거하여 비식별화한다")
    void deleteOrAnonymizeRequests() {
        // given
        InfoRegistrationRequest pending = pendingRequest(1L);
        InfoRegistrationRequest rejected = rejectedRequest(2L);
        InfoRegistrationRequest accepted = acceptedRequest(3L);
        given(readInfoRegistrationRequestPort.readAllByMemberId(REQUESTER_ID))
                .willReturn(List.of(pending, rejected, accepted));

        // when
        listener.deleteOrAnonymizeRequests(withdrawalEvent());

        // then
        then(saveInfoRegistrationRequestPort).should().deleteByIds(requestIdsCaptor.capture());
        assertEquals(List.of(pending.getId(), rejected.getId()), requestIdsCaptor.getValue());

        then(saveInfoRegistrationRequestPort).should().anonymizeAll(requestsCaptor.capture());
        List<InfoRegistrationRequest> anonymized = requestsCaptor.getValue();
        assertEquals(1, anonymized.size());
        assertEquals(accepted.getId(), anonymized.get(0).getId());
        assertNull(anonymized.get(0).getContactEmail());
    }

    @Test
    @DisplayName("비식별화된 요청은 제목, 내용, 상태, 연결된 정보가 유지된다")
    void keepAcceptedRequestContents() {
        // given
        given(readInfoRegistrationRequestPort.readAllByMemberId(REQUESTER_ID))
                .willReturn(List.of(acceptedRequest(1L)));

        // when
        listener.deleteOrAnonymizeRequests(withdrawalEvent());

        // then
        then(saveInfoRegistrationRequestPort).should().anonymizeAll(requestsCaptor.capture());
        InfoRegistrationRequest anonymized = requestsCaptor.getValue().get(0);
        assertEquals(REQUESTER_ID, anonymized.getRequesterId());
        assertEquals(TITLE, anonymized.getTitle());
        assertEquals(CONTENT, anonymized.getContent());
        assertEquals(InfoRequestStatus.ACCEPTED, anonymized.getStatus());
        assertEquals(INFO_ID, anonymized.getInfoId());
    }

    @Test
    @DisplayName("반영된 요청이 없으면 비식별화 대상은 빈 목록으로 전달된다")
    void noAcceptedRequests() {
        // given
        InfoRegistrationRequest pending = pendingRequest(1L);
        given(readInfoRegistrationRequestPort.readAllByMemberId(REQUESTER_ID)).willReturn(List.of(pending));

        // when
        listener.deleteOrAnonymizeRequests(withdrawalEvent());

        // then
        then(saveInfoRegistrationRequestPort).should().deleteByIds(List.of(pending.getId()));
        then(saveInfoRegistrationRequestPort).should().anonymizeAll(requestsCaptor.capture());
        assertTrue(requestsCaptor.getValue().isEmpty());
    }

    @Test
    @DisplayName("정보 등록 요청이 없으면 삭제·비식별화 대상은 모두 빈 목록으로 전달된다")
    void noRequests() {
        // given
        given(readInfoRegistrationRequestPort.readAllByMemberId(REQUESTER_ID)).willReturn(List.of());

        // when
        listener.deleteOrAnonymizeRequests(withdrawalEvent());

        // then
        then(saveInfoRegistrationRequestPort).should().deleteByIds(List.of());
        then(saveInfoRegistrationRequestPort).should().anonymizeAll(List.of());
    }

    private MemberWithdrawalEvent withdrawalEvent() {
        return new MemberWithdrawalEvent(REQUESTER_ID, List.of());
    }

    private InfoRegistrationRequest pendingRequest(Long id) {
        return request(id, InfoRequestStatus.PENDING, null, null);
    }

    private InfoRegistrationRequest rejectedRequest(Long id) {
        return request(id, InfoRequestStatus.REJECTED, null, REJECTION_REASON);
    }

    private InfoRegistrationRequest acceptedRequest(Long id) {
        return request(id, InfoRequestStatus.ACCEPTED, INFO_ID, null);
    }

    private InfoRegistrationRequest request(
            Long id,
            InfoRequestStatus status,
            InfoId infoId,
            RejectionReason rejectionReason
    ) {
        return InfoRegistrationRequest.withId(
                new InfoRegistrationRequestId(id),
                REQUESTER_ID,
                CATEGORY,
                TITLE,
                CONTENT,
                status,
                infoId,
                CONTACT_EMAIL,
                rejectionReason,
                REQUESTED_AT
        );
    }
}
