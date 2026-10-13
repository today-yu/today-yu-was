package com.todayyu.application.info.usecase;

import com.todayyu.application.info.dto.SubmitInfoUpdateRequestCommand;
import com.todayyu.application.info.dto.SubmitInfoUpdateRequestResult;
import com.todayyu.application.info.error.BusinessInfoErrorType;
import com.todayyu.application.info.port.read.ReadInfoPort;
import com.todayyu.application.info.port.save.SaveInfoUpdateRequestPort;
import com.todayyu.common.exception.CustomException;
import com.todayyu.domain.common.vo.Email;
import com.todayyu.domain.info.entity.InfoUpdateRequest;
import com.todayyu.domain.info.vo.InfoId;
import com.todayyu.domain.info.vo.InfoRequestStatus;
import com.todayyu.domain.info.vo.InfoUpdateRequestContent;
import com.todayyu.domain.info.vo.InfoUpdateRequestId;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class SubmitInfoUpdateRequestUsecaseTest {
    private static final MemberId REQUESTER_ID = new MemberId(1L);
    private static final InfoId INFO_ID = new InfoId(10L);
    private static final InfoUpdateRequestContent CONTENT = new InfoUpdateRequestContent("정보 수정 요청 내용");
    private static final Email CONTACT_EMAIL = new Email("user@example.com");
    private static final LocalDateTime OBSERVED_AT = LocalDateTime.of(2026, 10, 1, 9, 0);
    private static final SubmitInfoUpdateRequestCommand COMMAND =
            new SubmitInfoUpdateRequestCommand(REQUESTER_ID, INFO_ID, CONTENT, CONTACT_EMAIL, OBSERVED_AT);

    @Mock
    private ReadInfoPort readInfoPort;

    @Mock
    private SaveInfoUpdateRequestPort saveInfoUpdateRequestPort;

    @InjectMocks
    private SubmitInfoUpdateRequestUsecase submitInfoUpdateRequestUsecase;

    @Captor
    private ArgumentCaptor<InfoUpdateRequest> requestCaptor;

    @Test
    @DisplayName("입력한 요청자, 대상 정보, 내용, 연락용 이메일, 확인 시각으로 검토 중(PENDING) 상태의 정보 수정 요청을 저장한다")
    void submit() {
        // given
        given(readInfoPort.existsById(INFO_ID)).willReturn(true);
        given(saveInfoUpdateRequestPort.save(any())).willReturn(savedRequest(1L));
        LocalDateTime before = LocalDateTime.now();

        // when
        submitInfoUpdateRequestUsecase.submit(COMMAND);

        // then
        LocalDateTime after = LocalDateTime.now();
        then(saveInfoUpdateRequestPort).should().save(requestCaptor.capture());
        InfoUpdateRequest saved = requestCaptor.getValue();
        assertNull(saved.getId());
        assertEquals(REQUESTER_ID, saved.getRequesterId());
        assertEquals(INFO_ID, saved.getInfoId());
        assertEquals(CONTENT, saved.getContent());
        assertEquals(CONTACT_EMAIL, saved.getContactEmail());
        assertEquals(OBSERVED_AT, saved.getObservedAt());
        assertEquals(InfoRequestStatus.PENDING, saved.getStatus());
        assertNull(saved.getRejectionReason());
        assertFalse(saved.getRequestedAt().isBefore(before));
        assertFalse(saved.getRequestedAt().isAfter(after));
    }

    @Test
    @DisplayName("확인 시각이 없어도 정보 수정 요청을 저장한다")
    void submitWithoutObservedAt() {
        // given
        SubmitInfoUpdateRequestCommand command =
                new SubmitInfoUpdateRequestCommand(REQUESTER_ID, INFO_ID, CONTENT, CONTACT_EMAIL, null);
        given(readInfoPort.existsById(INFO_ID)).willReturn(true);
        given(saveInfoUpdateRequestPort.save(any())).willReturn(savedRequest(1L));

        // when
        submitInfoUpdateRequestUsecase.submit(command);

        // then
        then(saveInfoUpdateRequestPort).should().save(requestCaptor.capture());
        assertNull(requestCaptor.getValue().getObservedAt());
    }

    @Test
    @DisplayName("저장된 정보 수정 요청의 ID를 반환한다")
    void submitReturnsSavedId() {
        // given
        given(readInfoPort.existsById(INFO_ID)).willReturn(true);
        given(saveInfoUpdateRequestPort.save(any())).willReturn(savedRequest(7L));

        // when
        SubmitInfoUpdateRequestResult result = submitInfoUpdateRequestUsecase.submit(COMMAND);

        // then
        assertEquals(new InfoUpdateRequestId(7L), result.id());
    }

    @Test
    @DisplayName("대상 정보가 존재하지 않으면 예외가 발생하고 정보 수정 요청을 저장하지 않는다")
    void submitWithNonExistentInfo() {
        // given
        given(readInfoPort.existsById(INFO_ID)).willReturn(false);

        // when
        CustomException exception = assertThrows(CustomException.class,
                () -> submitInfoUpdateRequestUsecase.submit(COMMAND));

        // then
        assertEquals(BusinessInfoErrorType.NOT_FOUND, exception.getErrorType());
        then(saveInfoUpdateRequestPort).should(never()).save(any());
    }

    private InfoUpdateRequest savedRequest(Long id) {
        return InfoUpdateRequest.withId(
                new InfoUpdateRequestId(id),
                REQUESTER_ID,
                InfoRequestStatus.PENDING,
                INFO_ID,
                CONTENT,
                CONTACT_EMAIL,
                null,
                OBSERVED_AT,
                LocalDateTime.now()
        );
    }
}
