package com.todayyu.application.inquiry.usecase;

import com.todayyu.application.inquiry.dto.command.UpdateInquiryCommand;
import com.todayyu.application.inquiry.error.BusinessInquiryErrorType;
import com.todayyu.application.inquiry.port.read.ReadInquiryPort;
import com.todayyu.application.inquiry.port.save.SaveInquiryPort;
import com.todayyu.common.exception.CustomException;
import com.todayyu.domain.common.vo.Email;
import com.todayyu.domain.inquiry.entity.Inquiry;
import com.todayyu.domain.inquiry.error.InquiryErrorType;
import com.todayyu.domain.inquiry.vo.InquiryContent;
import com.todayyu.domain.inquiry.vo.InquiryId;
import com.todayyu.domain.inquiry.vo.InquiryStatus;
import com.todayyu.domain.inquiry.vo.InquiryTitle;
import com.todayyu.domain.member.vo.MemberId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class UpdateInquiryUsecaseTest {
    private static final InquiryId INQUIRY_ID = new InquiryId(1L);
    private static final MemberId AUTHOR_ID = new MemberId(1L);
    private static final MemberId OTHER_MEMBER_ID = new MemberId(2L);
    private static final Email CONTACT_EMAIL = new Email("user@example.com");
    private static final InquiryTitle TITLE = new InquiryTitle("문의 제목");
    private static final InquiryContent CONTENT = new InquiryContent("문의 내용");
    private static final Email NEW_CONTACT_EMAIL = new Email("new@example.com");
    private static final InquiryTitle NEW_TITLE = new InquiryTitle("수정된 문의 제목");
    private static final InquiryContent NEW_CONTENT = new InquiryContent("수정된 문의 내용");
    private static final LocalDateTime CREATED_AT = LocalDateTime.of(2026, 10, 1, 9, 0);
    private static final LocalDateTime UPDATED_AT = LocalDateTime.of(2026, 10, 2, 9, 0);
    private static final LocalDateTime ANSWERED_AT = LocalDateTime.of(2026, 10, 3, 9, 0);

    @Mock
    private ReadInquiryPort readInquiryPort;

    @Mock
    private SaveInquiryPort saveInquiryPort;

    @InjectMocks
    private UpdateInquiryUsecase updateInquiryUsecase;

    @Test
    @DisplayName("작성한 회원 본인이 답변 대기(WAITING) 상태의 문의를 수정한다")
    void update() {
        // given
        given(readInquiryPort.readById(INQUIRY_ID))
                .willReturn(Optional.of(inquiry(InquiryStatus.WAITING, null)));

        // when
        updateInquiryUsecase.update(command(AUTHOR_ID));

        // then
        ArgumentCaptor<Inquiry> captor = ArgumentCaptor.forClass(Inquiry.class);
        then(saveInquiryPort).should().update(captor.capture());

        Inquiry updated = captor.getValue();
        assertEquals(NEW_TITLE, updated.getTitle());
        assertEquals(NEW_CONTENT, updated.getContent());
        assertEquals(NEW_CONTACT_EMAIL, updated.getContactEmail());
        assertEquals(CREATED_AT, updated.getCreatedAt());
        assertTrue(updated.getUpdatedAt().isAfter(UPDATED_AT));
    }

    @Test
    @DisplayName("문의가 존재하지 않으면 NOT_FOUND 예외가 발생하고 수정하지 않는다")
    void updateWithNotExistingInquiry() {
        // given
        given(readInquiryPort.readById(INQUIRY_ID)).willReturn(Optional.empty());

        // when
        CustomException exception = assertThrows(CustomException.class,
                () -> updateInquiryUsecase.update(command(AUTHOR_ID)));

        // then
        assertEquals(BusinessInquiryErrorType.NOT_FOUND, exception.getErrorType());
        then(saveInquiryPort).should(never()).update(any());
    }

    @Test
    @DisplayName("작성한 회원 본인이 아니면 FORBIDDEN 예외가 발생하고 수정하지 않는다")
    void updateByOtherMember() {
        // given
        given(readInquiryPort.readById(INQUIRY_ID))
                .willReturn(Optional.of(inquiry(InquiryStatus.WAITING, null)));

        // when
        CustomException exception = assertThrows(CustomException.class,
                () -> updateInquiryUsecase.update(command(OTHER_MEMBER_ID)));

        // then
        assertEquals(BusinessInquiryErrorType.FORBIDDEN, exception.getErrorType());
        then(saveInquiryPort).should(never()).update(any());
    }

    @Test
    @DisplayName("답변 완료(ANSWERED)된 문의이면 FAIL_UPDATE 예외가 발생하고 수정하지 않는다")
    void updateAnsweredInquiry() {
        // given
        given(readInquiryPort.readById(INQUIRY_ID))
                .willReturn(Optional.of(inquiry(InquiryStatus.ANSWERED, ANSWERED_AT)));

        // when
        CustomException exception = assertThrows(CustomException.class,
                () -> updateInquiryUsecase.update(command(AUTHOR_ID)));

        // then
        assertEquals(InquiryErrorType.FAIL_UPDATE, exception.getErrorType());
        then(saveInquiryPort).should(never()).update(any());
    }

    private UpdateInquiryCommand command(MemberId memberId) {
        return new UpdateInquiryCommand(INQUIRY_ID, NEW_TITLE, NEW_CONTENT, NEW_CONTACT_EMAIL, memberId);
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
