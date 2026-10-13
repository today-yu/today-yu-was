package com.todayyu.application.info.usecase;

import com.todayyu.application.info.dto.command.SubmitInfoRegistrationRequestCommand;
import com.todayyu.application.info.dto.result.SubmitInfoRegistrationRequestResult;
import com.todayyu.application.info.port.save.SaveInfoRegistrationRequestPort;
import com.todayyu.domain.common.vo.Email;
import com.todayyu.domain.info.entity.InfoRegistrationRequest;
import com.todayyu.domain.info.vo.InfoCategory;
import com.todayyu.domain.info.vo.InfoRegistrationRequestContent;
import com.todayyu.domain.info.vo.InfoRegistrationRequestId;
import com.todayyu.domain.info.vo.InfoRequestStatus;
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
class SubmitInfoRegistrationRequestUsecaseTest {
    private static final MemberId REQUESTER_ID = new MemberId(1L);
    private static final InfoCategory CATEGORY = InfoCategory.ACADEMIC;
    private static final InfoRegistrationRequestContent CONTENT = new InfoRegistrationRequestContent("정보 등록 요청 내용");
    private static final Email CONTACT_EMAIL = new Email("user@example.com");
    private static final SubmitInfoRegistrationRequestCommand COMMAND =
            new SubmitInfoRegistrationRequestCommand(REQUESTER_ID, CATEGORY, CONTENT, CONTACT_EMAIL);

    @Mock
    private SaveInfoRegistrationRequestPort saveInfoRegistrationRequestPort;

    @InjectMocks
    private SubmitInfoRegistrationRequestUsecase submitInfoRegistrationRequestUsecase;

    @Captor
    private ArgumentCaptor<InfoRegistrationRequest> requestCaptor;

    @Test
    @DisplayName("입력한 요청자, 카테고리, 내용, 연락용 이메일로 검토 중(PENDING) 상태의 정보 등록 요청을 저장한다")
    void submit() {
        // given
        given(saveInfoRegistrationRequestPort.save(any())).willReturn(savedRequest(1L));
        LocalDateTime before = LocalDateTime.now();

        // when
        submitInfoRegistrationRequestUsecase.submit(COMMAND);

        // then
        LocalDateTime after = LocalDateTime.now();
        then(saveInfoRegistrationRequestPort).should().save(requestCaptor.capture());
        InfoRegistrationRequest saved = requestCaptor.getValue();
        assertNull(saved.getId());
        assertEquals(REQUESTER_ID, saved.getRequesterId());
        assertEquals(CATEGORY, saved.getCategory());
        assertEquals(CONTENT, saved.getContent());
        assertEquals(CONTACT_EMAIL, saved.getContactEmail());
        assertEquals(InfoRequestStatus.PENDING, saved.getStatus());
        assertNull(saved.getInfoId());
        assertNull(saved.getRejectionReason());
        assertFalse(saved.getRequestedAt().isBefore(before));
        assertFalse(saved.getRequestedAt().isAfter(after));
    }

    @Test
    @DisplayName("저장된 정보 등록 요청의 ID를 반환한다")
    void submitReturnsSavedId() {
        // given
        given(saveInfoRegistrationRequestPort.save(any())).willReturn(savedRequest(7L));

        // when
        SubmitInfoRegistrationRequestResult result = submitInfoRegistrationRequestUsecase.submit(COMMAND);

        // then
        assertEquals(new InfoRegistrationRequestId(7L), result.id());
    }

    private InfoRegistrationRequest savedRequest(Long id) {
        return InfoRegistrationRequest.withId(
                new InfoRegistrationRequestId(id),
                REQUESTER_ID,
                CATEGORY,
                CONTENT,
                InfoRequestStatus.PENDING,
                null,
                CONTACT_EMAIL,
                null,
                LocalDateTime.now()
        );
    }
}
