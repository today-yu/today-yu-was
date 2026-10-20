package com.todayyu.application.inquiry.usecase;

import com.todayyu.application.inquiry.dto.command.SubmitInquiryCommand;
import com.todayyu.application.inquiry.dto.result.SubmitInquiryResult;
import com.todayyu.application.inquiry.port.save.SaveInquiryPort;
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
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class SubmitInquiryUsecaseTest {
    private static final MemberId MEMBER_ID = new MemberId(1L);
    private static final Email CONTACT_EMAIL = new Email("user@example.com");
    private static final InquiryTitle TITLE = new InquiryTitle("문의 제목");
    private static final InquiryContent CONTENT = new InquiryContent("문의 내용");
    private static final SubmitInquiryCommand COMMAND =
            new SubmitInquiryCommand(MEMBER_ID, CONTACT_EMAIL, TITLE, CONTENT);

    @Mock
    private SaveInquiryPort saveInquiryPort;

    @InjectMocks
    private SubmitInquiryUsecase submitInquiryUsecase;

    @Captor
    private ArgumentCaptor<Inquiry> inquiryCaptor;

    @Test
    @DisplayName("입력한 회원, 연락용 이메일, 제목, 내용으로 답변 대기(WAITING) 상태의 문의를 저장한다")
    void submit() {
        // given
        given(saveInquiryPort.save(any())).willReturn(savedInquiry(1L));
        LocalDateTime before = LocalDateTime.now();

        // when
        submitInquiryUsecase.submit(COMMAND);

        // then
        LocalDateTime after = LocalDateTime.now();
        then(saveInquiryPort).should().save(inquiryCaptor.capture());
        Inquiry saved = inquiryCaptor.getValue();
        assertNull(saved.getId());
        assertEquals(MEMBER_ID, saved.getMemberId());
        assertEquals(CONTACT_EMAIL, saved.getContactEmail());
        assertEquals(TITLE, saved.getTitle());
        assertEquals(CONTENT, saved.getContent());
        assertEquals(InquiryStatus.WAITING, saved.getStatus());
        assertNull(saved.getAnsweredAt());
        assertFalse(saved.getCreatedAt().isBefore(before));
        assertFalse(saved.getCreatedAt().isAfter(after));
        assertEquals(saved.getCreatedAt(), saved.getUpdatedAt());
    }

    @Test
    @DisplayName("저장된 문의의 ID를 반환한다")
    void submitReturnsSavedId() {
        // given
        given(saveInquiryPort.save(any())).willReturn(savedInquiry(7L));

        // when
        SubmitInquiryResult result = submitInquiryUsecase.submit(COMMAND);

        // then
        assertEquals(new InquiryId(7L), result.id());
    }

    private Inquiry savedInquiry(Long id) {
        LocalDateTime now = LocalDateTime.now();
        return Inquiry.withId(
                new InquiryId(id),
                MEMBER_ID,
                CONTACT_EMAIL,
                InquiryStatus.WAITING,
                TITLE,
                CONTENT,
                now,
                now,
                null
        );
    }
}
