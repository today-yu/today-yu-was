package com.todayyu.application.inquiry.usecase;

import com.todayyu.application.inquiry.dto.query.GetInquiryDetailQuery;
import com.todayyu.application.inquiry.dto.result.InquiryDetailResult;
import com.todayyu.application.inquiry.error.BusinessInquiryErrorType;
import com.todayyu.application.inquiry.port.read.ReadInquiryPort;
import com.todayyu.common.exception.CustomException;
import com.todayyu.domain.common.vo.Email;
import com.todayyu.domain.inquiry.entity.Inquiry;
import com.todayyu.domain.inquiry.vo.InquiryContent;
import com.todayyu.domain.inquiry.vo.InquiryId;
import com.todayyu.domain.inquiry.vo.InquiryStatus;
import com.todayyu.domain.inquiry.vo.InquiryTitle;
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
class GetInquiryDetailUsecaseTest {
    private static final InquiryId INQUIRY_ID = new InquiryId(1L);
    private static final MemberId AUTHOR_ID = new MemberId(1L);
    private static final MemberId OTHER_MEMBER_ID = new MemberId(2L);
    private static final Email CONTACT_EMAIL = new Email("user@example.com");
    private static final InquiryTitle TITLE = new InquiryTitle("문의 제목");
    private static final InquiryContent CONTENT = new InquiryContent("문의 내용");
    private static final LocalDateTime CREATED_AT = LocalDateTime.of(2026, 10, 1, 9, 0);
    private static final LocalDateTime UPDATED_AT = LocalDateTime.of(2026, 10, 2, 9, 0);
    private static final LocalDateTime ANSWERED_AT = LocalDateTime.of(2026, 10, 3, 9, 0);

    @Mock
    private ReadInquiryPort readInquiryPort;

    @InjectMocks
    private GetInquiryDetailUsecase getInquiryDetailUsecase;

    @Test
    @DisplayName("작성한 회원 본인이 조회하면 답변 대기(WAITING) 상태 문의의 상세 내용을 반환한다")
    void getInquiryDetail() {
        // given
        given(readInquiryPort.readById(INQUIRY_ID))
                .willReturn(Optional.of(inquiry(InquiryStatus.WAITING, null)));

        // when
        InquiryDetailResult result = getInquiryDetailUsecase.getInquiryDetail(query(AUTHOR_ID));

        // then
        assertEquals(INQUIRY_ID, result.id());
        assertEquals(AUTHOR_ID, result.memberId());
        assertEquals(CONTACT_EMAIL, result.contactEmail());
        assertEquals(InquiryStatus.WAITING, result.status());
        assertEquals(TITLE, result.title());
        assertEquals(CONTENT, result.content());
        assertEquals(CREATED_AT, result.createdAt());
        assertEquals(UPDATED_AT, result.updatedAt());
        assertNull(result.answeredAt());
    }

    @Test
    @DisplayName("답변 완료(ANSWERED)된 문의이면 답변 시각을 함께 반환한다")
    void getAnsweredInquiryDetail() {
        // given
        given(readInquiryPort.readById(INQUIRY_ID))
                .willReturn(Optional.of(inquiry(InquiryStatus.ANSWERED, ANSWERED_AT)));

        // when
        InquiryDetailResult result = getInquiryDetailUsecase.getInquiryDetail(query(AUTHOR_ID));

        // then
        assertEquals(InquiryStatus.ANSWERED, result.status());
        assertEquals(ANSWERED_AT, result.answeredAt());
    }

    @Test
    @DisplayName("문의가 존재하지 않으면 NOT_FOUND 예외가 발생한다")
    void getInquiryDetailWithNotExistingInquiry() {
        // given
        given(readInquiryPort.readById(INQUIRY_ID)).willReturn(Optional.empty());

        // when
        CustomException exception = assertThrows(CustomException.class,
                () -> getInquiryDetailUsecase.getInquiryDetail(query(AUTHOR_ID)));

        // then
        assertEquals(BusinessInquiryErrorType.NOT_FOUND, exception.getErrorType());
    }

    @Test
    @DisplayName("작성한 회원 본인이 아니면 FORBIDDEN 예외가 발생한다")
    void getInquiryDetailByOtherMember() {
        // given
        given(readInquiryPort.readById(INQUIRY_ID))
                .willReturn(Optional.of(inquiry(InquiryStatus.WAITING, null)));

        // when
        CustomException exception = assertThrows(CustomException.class,
                () -> getInquiryDetailUsecase.getInquiryDetail(query(OTHER_MEMBER_ID)));

        // then
        assertEquals(BusinessInquiryErrorType.FORBIDDEN, exception.getErrorType());
    }

    private GetInquiryDetailQuery query(MemberId memberId) {
        return new GetInquiryDetailQuery(memberId, INQUIRY_ID);
    }

    private Inquiry inquiry(InquiryStatus status, LocalDateTime answeredAt) {
        return Inquiry.withId(
                INQUIRY_ID,
                AUTHOR_ID,
                CONTACT_EMAIL,
                status,
                TITLE,
                CONTENT,
                CREATED_AT,
                UPDATED_AT,
                answeredAt
        );
    }
}
