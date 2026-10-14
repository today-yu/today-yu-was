package com.todayyu.application.info.usecase;

import com.todayyu.application.info.dto.query.GetInfoUpdateRequestDetailQuery;
import com.todayyu.application.info.dto.result.InfoUpdateRequestDetailResult;
import com.todayyu.application.info.error.BusinessInfoUpdateRequestErrorType;
import com.todayyu.application.info.port.read.ReadInfoPort;
import com.todayyu.application.info.port.read.ReadInfoUpdateRequestPort;
import com.todayyu.common.exception.CustomException;
import com.todayyu.domain.common.vo.Email;
import com.todayyu.domain.info.entity.Info;
import com.todayyu.domain.info.entity.InfoUpdateRequest;
import com.todayyu.domain.info.vo.InfoCategory;
import com.todayyu.domain.info.vo.InfoContent;
import com.todayyu.domain.info.vo.InfoId;
import com.todayyu.domain.info.vo.InfoRequestStatus;
import com.todayyu.domain.info.vo.InfoTitle;
import com.todayyu.domain.info.vo.InfoUpdateRequestContent;
import com.todayyu.domain.info.vo.InfoUpdateRequestId;
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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class GetInfoUpdateRequestDetailUsecaseTest {
    private static final InfoUpdateRequestId REQUEST_ID = new InfoUpdateRequestId(1L);
    private static final MemberId REQUESTER_ID = new MemberId(1L);
    private static final MemberId OTHER_MEMBER_ID = new MemberId(2L);
    private static final InfoId INFO_ID = new InfoId(10L);
    private static final InfoCategory INFO_CATEGORY = InfoCategory.ACADEMIC;
    private static final InfoTitle INFO_TITLE = new InfoTitle("수강신청 일정");
    private static final LocalDateTime INFO_CREATED_AT = LocalDateTime.of(2026, 9, 1, 9, 0);
    private static final LocalDateTime INFO_UPDATED_AT = LocalDateTime.of(2026, 9, 15, 12, 0);
    private static final InfoUpdateRequestContent CONTENT = new InfoUpdateRequestContent("정보 수정 요청 내용");
    private static final Email CONTACT_EMAIL = new Email("user@example.com");
    private static final LocalDateTime OBSERVED_AT = LocalDateTime.of(2026, 9, 30, 18, 0);
    private static final LocalDateTime REQUESTED_AT = LocalDateTime.of(2026, 10, 1, 9, 0);
    private static final RejectionReason REJECTION_REASON = new RejectionReason("사실 관계를 확인할 수 없습니다.");

    @Mock
    private ReadInfoUpdateRequestPort readInfoUpdateRequestPort;

    @Mock
    private ReadInfoPort readInfoPort;

    @InjectMocks
    private GetInfoUpdateRequestDetailUsecase getInfoUpdateRequestDetailUsecase;

    @Test
    @DisplayName("요청한 회원 본인이 조회하면 연결된 정보의 ID, 카테고리, 제목, 최근 갱신 일시를 포함한 정보 수정 요청의 상세 내용을 반환한다")
    void getInfoUpdateRequestDetail() {
        // given
        given(readInfoUpdateRequestPort.readById(REQUEST_ID))
                .willReturn(Optional.of(request(InfoRequestStatus.PENDING, null)));
        given(readInfoPort.readById(INFO_ID)).willReturn(Optional.of(info()));

        // when
        InfoUpdateRequestDetailResult result = getInfoUpdateRequestDetailUsecase
                .getInfoUpdateRequestDetail(query(REQUESTER_ID));

        // then
        assertEquals(REQUEST_ID, result.id());
        assertEquals(REQUESTER_ID, result.requesterId());
        assertEquals(CONTENT, result.content());
        assertEquals(CONTACT_EMAIL, result.contactEmail());
        assertEquals(OBSERVED_AT, result.observedAt());
        assertEquals(REQUESTED_AT, result.requestedAt());
        assertEquals(InfoRequestStatus.PENDING, result.status());
        assertNotNull(result.info());
        assertEquals(INFO_ID, result.info().id());
        assertEquals(INFO_CATEGORY, result.info().category());
        assertEquals(INFO_TITLE, result.info().title());
        assertEquals(INFO_UPDATED_AT, result.info().updatedAt());
        assertNull(result.rejectionReason());
    }

    @Test
    @DisplayName("연결된 정보가 존재하지 않으면 연결된 정보 없이 상세 내용을 반환한다")
    void getInfoUpdateRequestDetailWithNotExistingInfo() {
        // given
        given(readInfoUpdateRequestPort.readById(REQUEST_ID))
                .willReturn(Optional.of(request(InfoRequestStatus.PENDING, null)));
        given(readInfoPort.readById(INFO_ID)).willReturn(Optional.empty());

        // when
        InfoUpdateRequestDetailResult result = getInfoUpdateRequestDetailUsecase
                .getInfoUpdateRequestDetail(query(REQUESTER_ID));

        // then
        assertEquals(REQUEST_ID, result.id());
        assertEquals(InfoRequestStatus.PENDING, result.status());
        assertNull(result.info());
    }

    @Test
    @DisplayName("반려(REJECTED)된 정보 수정 요청이면 반려 사유를 함께 반환한다")
    void getRejectedInfoUpdateRequestDetail() {
        // given
        given(readInfoUpdateRequestPort.readById(REQUEST_ID))
                .willReturn(Optional.of(request(InfoRequestStatus.REJECTED, REJECTION_REASON)));
        given(readInfoPort.readById(INFO_ID)).willReturn(Optional.of(info()));

        // when
        InfoUpdateRequestDetailResult result = getInfoUpdateRequestDetailUsecase
                .getInfoUpdateRequestDetail(query(REQUESTER_ID));

        // then
        assertEquals(InfoRequestStatus.REJECTED, result.status());
        assertEquals(INFO_ID, result.info().id());
        assertEquals(REJECTION_REASON, result.rejectionReason());
    }

    @Test
    @DisplayName("정보 수정 요청이 존재하지 않으면 NOT_FOUND 예외가 발생한다")
    void getInfoUpdateRequestDetailWithNotExistingRequest() {
        // given
        given(readInfoUpdateRequestPort.readById(REQUEST_ID)).willReturn(Optional.empty());

        // when
        CustomException exception = assertThrows(CustomException.class,
                () -> getInfoUpdateRequestDetailUsecase.getInfoUpdateRequestDetail(query(REQUESTER_ID)));

        // then
        assertEquals(BusinessInfoUpdateRequestErrorType.NOT_FOUND, exception.getErrorType());
    }

    @Test
    @DisplayName("요청한 회원 본인이 아니면 FORBIDDEN 예외가 발생한다")
    void getInfoUpdateRequestDetailByOtherMember() {
        // given
        given(readInfoUpdateRequestPort.readById(REQUEST_ID))
                .willReturn(Optional.of(request(InfoRequestStatus.PENDING, null)));

        // when
        CustomException exception = assertThrows(CustomException.class,
                () -> getInfoUpdateRequestDetailUsecase.getInfoUpdateRequestDetail(query(OTHER_MEMBER_ID)));

        // then
        assertEquals(BusinessInfoUpdateRequestErrorType.FORBIDDEN, exception.getErrorType());
        then(readInfoPort).should(never()).readById(any());
    }

    private GetInfoUpdateRequestDetailQuery query(MemberId memberId) {
        return new GetInfoUpdateRequestDetailQuery(REQUEST_ID, memberId);
    }

    private Info info() {
        return Info.withId(
                INFO_ID,
                INFO_CATEGORY,
                INFO_TITLE,
                new InfoContent("수강신청은 10월 20일부터 진행됩니다."),
                INFO_CREATED_AT,
                INFO_UPDATED_AT
        );
    }

    private InfoUpdateRequest request(InfoRequestStatus status, RejectionReason rejectionReason) {
        return InfoUpdateRequest.withId(
                REQUEST_ID,
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
