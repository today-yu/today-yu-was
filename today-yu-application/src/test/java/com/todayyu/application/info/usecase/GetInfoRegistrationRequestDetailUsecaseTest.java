package com.todayyu.application.info.usecase;

import com.todayyu.application.info.dto.query.GetInfoRegistrationRequestDetailQuery;
import com.todayyu.application.info.dto.result.InfoRegistrationRequestDetailResult;
import com.todayyu.application.info.error.BusinessInfoRegistrationRequestErrorType;
import com.todayyu.application.info.port.read.ReadInfoRegistrationRequestPort;
import com.todayyu.common.exception.CustomException;
import com.todayyu.domain.common.vo.Email;
import com.todayyu.domain.info.entity.InfoRegistrationRequest;
import com.todayyu.domain.info.vo.InfoCategory;
import com.todayyu.domain.info.vo.InfoId;
import com.todayyu.domain.info.vo.InfoRegistrationRequestContent;
import com.todayyu.domain.info.vo.InfoRegistrationRequestId;
import com.todayyu.domain.info.vo.InfoRequestStatus;
import com.todayyu.domain.info.vo.RejectionReason;
import com.todayyu.domain.member.vo.MemberId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class GetInfoRegistrationRequestDetailUsecaseTest {
    private static final InfoRegistrationRequestId REQUEST_ID = new InfoRegistrationRequestId(1L);
    private static final MemberId REQUESTER_ID = new MemberId(1L);
    private static final MemberId OTHER_MEMBER_ID = new MemberId(2L);
    private static final InfoCategory CATEGORY = InfoCategory.ACADEMIC;
    private static final InfoRegistrationRequestContent CONTENT = new InfoRegistrationRequestContent("정보 등록 요청 내용");
    private static final Email CONTACT_EMAIL = new Email("user@example.com");
    private static final LocalDateTime REQUESTED_AT = LocalDateTime.of(2026, 10, 1, 9, 0);
    private static final InfoId INFO_ID = new InfoId(10L);
    private static final RejectionReason REJECTION_REASON = new RejectionReason("사실 관계를 확인할 수 없습니다.");

    @Mock
    private ReadInfoRegistrationRequestPort readInfoRegistrationRequestPort;

    @InjectMocks
    private GetInfoRegistrationRequestDetailUsecase getInfoRegistrationRequestDetailUsecase;

    @Test
    @DisplayName("요청한 회원 본인이 조회하면 정보 등록 요청의 상세 내용을 반환한다")
    void getInfoRegistrationRequestDetail() {
        // given
        given(readInfoRegistrationRequestPort.readById(REQUEST_ID))
                .willReturn(Optional.of(request(InfoRequestStatus.PENDING, null, null)));

        // when
        InfoRegistrationRequestDetailResult result = getInfoRegistrationRequestDetailUsecase
                .getInfoRegistrationRequestDetail(query(REQUESTER_ID));

        // then
        assertEquals(REQUEST_ID, result.id());
        assertEquals(REQUESTER_ID, result.requesterId());
        assertEquals(CATEGORY, result.category());
        assertEquals(CONTENT, result.content());
        assertEquals(CONTACT_EMAIL, result.contactEmail());
        assertEquals(REQUESTED_AT, result.requestedAt());
        assertEquals(InfoRequestStatus.PENDING, result.status());
        assertNull(result.infoId());
        assertNull(result.rejectionReason());
    }

    @Test
    @DisplayName("반영(ACCEPTED)된 정보 등록 요청이면 반영된 정보 ID를 함께 반환한다")
    void getAcceptedInfoRegistrationRequestDetail() {
        // given
        given(readInfoRegistrationRequestPort.readById(REQUEST_ID))
                .willReturn(Optional.of(request(InfoRequestStatus.ACCEPTED, INFO_ID, null)));

        // when
        InfoRegistrationRequestDetailResult result = getInfoRegistrationRequestDetailUsecase
                .getInfoRegistrationRequestDetail(query(REQUESTER_ID));

        // then
        assertEquals(InfoRequestStatus.ACCEPTED, result.status());
        assertEquals(INFO_ID, result.infoId());
        assertNull(result.rejectionReason());
    }

    @Test
    @DisplayName("반려(REJECTED)된 정보 등록 요청이면 반려 사유를 함께 반환한다")
    void getRejectedInfoRegistrationRequestDetail() {
        // given
        given(readInfoRegistrationRequestPort.readById(REQUEST_ID))
                .willReturn(Optional.of(request(InfoRequestStatus.REJECTED, null, REJECTION_REASON)));

        // when
        InfoRegistrationRequestDetailResult result = getInfoRegistrationRequestDetailUsecase
                .getInfoRegistrationRequestDetail(query(REQUESTER_ID));

        // then
        assertEquals(InfoRequestStatus.REJECTED, result.status());
        assertNull(result.infoId());
        assertEquals(REJECTION_REASON, result.rejectionReason());
    }

    @Test
    @DisplayName("정보 등록 요청이 존재하지 않으면 NOT_FOUND 예외가 발생한다")
    void getInfoRegistrationRequestDetailWithNotExistingRequest() {
        // given
        given(readInfoRegistrationRequestPort.readById(REQUEST_ID)).willReturn(Optional.empty());

        // when
        CustomException exception = assertThrows(CustomException.class,
                () -> getInfoRegistrationRequestDetailUsecase.getInfoRegistrationRequestDetail(query(REQUESTER_ID)));

        // then
        assertEquals(BusinessInfoRegistrationRequestErrorType.NOT_FOUND, exception.getErrorType());
    }

    @Test
    @DisplayName("요청한 회원 본인이 아니면 FORBIDDEN 예외가 발생한다")
    void getInfoRegistrationRequestDetailByOtherMember() {
        // given
        given(readInfoRegistrationRequestPort.readById(REQUEST_ID))
                .willReturn(Optional.of(request(InfoRequestStatus.PENDING, null, null)));

        // when
        CustomException exception = assertThrows(CustomException.class,
                () -> getInfoRegistrationRequestDetailUsecase.getInfoRegistrationRequestDetail(query(OTHER_MEMBER_ID)));

        // then
        assertEquals(BusinessInfoRegistrationRequestErrorType.FORBIDDEN, exception.getErrorType());
    }

    private GetInfoRegistrationRequestDetailQuery query(MemberId memberId) {
        return new GetInfoRegistrationRequestDetailQuery(REQUEST_ID, memberId);
    }

    private InfoRegistrationRequest request(InfoRequestStatus status, InfoId infoId, RejectionReason rejectionReason) {
        return InfoRegistrationRequest.withId(
                REQUEST_ID,
                REQUESTER_ID,
                CATEGORY,
                CONTENT,
                status,
                infoId,
                CONTACT_EMAIL,
                rejectionReason,
                REQUESTED_AT
        );
    }
}
