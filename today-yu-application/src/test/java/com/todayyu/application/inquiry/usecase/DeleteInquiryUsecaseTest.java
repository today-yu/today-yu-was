package com.todayyu.application.inquiry.usecase;

import com.todayyu.application.inquiry.dto.command.DeleteInquiryCommand;
import com.todayyu.application.inquiry.error.BusinessInquiryErrorType;
import com.todayyu.application.inquiry.port.read.ReadInquiryPort;
import com.todayyu.application.inquiry.port.save.SaveInquiryPort;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class DeleteInquiryUsecaseTest {
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

    @Mock
    private SaveInquiryPort saveInquiryPort;

    @InjectMocks
    private DeleteInquiryUsecase deleteInquiryUsecase;

    @Test
    @DisplayName("작성한 회원 본인이 답변 대기(WAITING) 상태의 문의를 삭제한다")
    void delete() {
        // given
        given(readInquiryPort.readById(INQUIRY_ID))
                .willReturn(Optional.of(inquiry(InquiryStatus.WAITING, null)));

        // when
        deleteInquiryUsecase.delete(command(AUTHOR_ID));

        // then
        then(saveInquiryPort).should().deleteById(INQUIRY_ID);
    }

    @Test
    @DisplayName("문의가 존재하지 않으면 NOT_FOUND 예외가 발생하고 삭제하지 않는다")
    void deleteWithNotExistingInquiry() {
        // given
        given(readInquiryPort.readById(INQUIRY_ID)).willReturn(Optional.empty());

        // when
        CustomException exception = assertThrows(CustomException.class,
                () -> deleteInquiryUsecase.delete(command(AUTHOR_ID)));

        // then
        assertEquals(BusinessInquiryErrorType.NOT_FOUND, exception.getErrorType());
        then(saveInquiryPort).should(never()).deleteById(any());
    }

    @Test
    @DisplayName("작성한 회원 본인이 아니면 FORBIDDEN 예외가 발생하고 삭제하지 않는다")
    void deleteByOtherMember() {
        // given
        given(readInquiryPort.readById(INQUIRY_ID))
                .willReturn(Optional.of(inquiry(InquiryStatus.WAITING, null)));

        // when
        CustomException exception = assertThrows(CustomException.class,
                () -> deleteInquiryUsecase.delete(command(OTHER_MEMBER_ID)));

        // then
        assertEquals(BusinessInquiryErrorType.FORBIDDEN, exception.getErrorType());
        then(saveInquiryPort).should(never()).deleteById(any());
    }

    @Test
    @DisplayName("답변 완료(ANSWERED)된 문의이면 FAIL_DELETE 예외가 발생하고 삭제하지 않는다")
    void deleteAnsweredInquiry() {
        // given
        given(readInquiryPort.readById(INQUIRY_ID))
                .willReturn(Optional.of(inquiry(InquiryStatus.ANSWERED, ANSWERED_AT)));

        // when
        CustomException exception = assertThrows(CustomException.class,
                () -> deleteInquiryUsecase.delete(command(AUTHOR_ID)));

        // then
        assertEquals(BusinessInquiryErrorType.FAIL_DELETE, exception.getErrorType());
        then(saveInquiryPort).should(never()).deleteById(any());
    }

    private DeleteInquiryCommand command(MemberId memberId) {
        return new DeleteInquiryCommand(INQUIRY_ID, memberId);
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
