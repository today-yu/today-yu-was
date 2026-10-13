package com.todayyu.apis.user.info.controller;

import com.todayyu.apis.common.exception.GlobalExceptionHandler;
import com.todayyu.apis.common.security.principal.CustomUserDetails;
import com.todayyu.application.info.dto.SubmitInfoUpdateRequestCommand;
import com.todayyu.application.info.dto.SubmitInfoUpdateRequestResult;
import com.todayyu.application.info.error.BusinessInfoErrorType;
import com.todayyu.application.info.usecase.SubmitInfoUpdateRequestUsecase;
import com.todayyu.common.error.CommonErrorType;
import com.todayyu.common.exception.CustomException;
import com.todayyu.domain.common.vo.Email;
import com.todayyu.domain.info.vo.InfoId;
import com.todayyu.domain.info.vo.InfoUpdateRequestContent;
import com.todayyu.domain.info.vo.InfoUpdateRequestId;
import com.todayyu.domain.member.vo.MemberId;
import com.todayyu.domain.member.vo.Role;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class InfoUpdateRequestControllerTest {
    private static final String SUBMIT_URL = "/api/v1/infos/{infoId}/update-requests";
    private static final Long MEMBER_ID = 1L;
    private static final Long INFO_ID = 5L;
    private static final Long REQUEST_ID = 10L;
    private static final String CONTENT = "수강신청 일정이 10월 21일로 변경되었습니다.";
    private static final String CONTACT_EMAIL = "requester@example.com";
    private static final String OBSERVED_AT = "2026-10-01T09:00:00";

    @Mock
    private SubmitInfoUpdateRequestUsecase submitInfoUpdateRequestUsecase;

    @InjectMocks
    private InfoUpdateRequestController infoUpdateRequestController;

    @Captor
    private ArgumentCaptor<SubmitInfoUpdateRequestCommand> commandCaptor;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(infoUpdateRequestController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .build();

        CustomUserDetails userDetails = new CustomUserDetails(new MemberId(MEMBER_ID), Role.MEMBER);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities())
        );
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("정보 수정 요청 제출 시 경로의 정보 ID, 인증된 회원 ID, 요청 본문으로 제출을 요청하고, 생성된 정보 수정 요청 ID를 반환한다")
    void submit() throws Exception {
        // given
        given(submitInfoUpdateRequestUsecase.submit(any()))
                .willReturn(new SubmitInfoUpdateRequestResult(new InfoUpdateRequestId(REQUEST_ID)));

        // when
        mockMvc.perform(post(SUBMIT_URL, INFO_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(CONTENT, CONTACT_EMAIL, OBSERVED_AT)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("정보 수정 요청 제출에 성공하였습니다."))
                .andExpect(jsonPath("$.data.infoUpdateRequestId").value(REQUEST_ID));

        // then
        then(submitInfoUpdateRequestUsecase).should().submit(commandCaptor.capture());
        SubmitInfoUpdateRequestCommand command = commandCaptor.getValue();
        assertEquals(new MemberId(MEMBER_ID), command.requesterId());
        assertEquals(new InfoId(INFO_ID), command.infoId());
        assertEquals(new InfoUpdateRequestContent(CONTENT), command.content());
        assertEquals(new Email(CONTACT_EMAIL), command.contactEmail());
        assertEquals(LocalDateTime.of(2026, 10, 1, 9, 0), command.observedAt());
    }

    @Test
    @DisplayName("요청 본문에 요청자 ID와 정보 ID가 포함되어도 무시하고 인증된 회원 ID와 경로의 정보 ID로 제출을 요청한다")
    void submitIgnoresRequesterIdAndInfoIdInBody() throws Exception {
        // given
        given(submitInfoUpdateRequestUsecase.submit(any()))
                .willReturn(new SubmitInfoUpdateRequestResult(new InfoUpdateRequestId(REQUEST_ID)));

        // when
        mockMvc.perform(post(SUBMIT_URL, INFO_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"requesterId": 999, "infoId": 999, "content": "%s", "contactEmail": "%s", "observedAt": "%s"}
                                """.formatted(CONTENT, CONTACT_EMAIL, OBSERVED_AT)))
                .andExpect(status().isCreated());

        // then
        then(submitInfoUpdateRequestUsecase).should().submit(commandCaptor.capture());
        assertEquals(new MemberId(MEMBER_ID), commandCaptor.getValue().requesterId());
        assertEquals(new InfoId(INFO_ID), commandCaptor.getValue().infoId());
    }

    @Test
    @DisplayName("대상 정보가 존재하지 않으면 정보 없음 에러를 응답한다")
    void submitWithNonExistentInfo() throws Exception {
        // given
        given(submitInfoUpdateRequestUsecase.submit(any()))
                .willThrow(new CustomException(BusinessInfoErrorType.NOT_FOUND));

        // when & then
        mockMvc.perform(post(SUBMIT_URL, INFO_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(CONTENT, CONTACT_EMAIL, OBSERVED_AT)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value(BusinessInfoErrorType.NOT_FOUND.getErrorCode()))
                .andExpect(jsonPath("$.message").value(BusinessInfoErrorType.NOT_FOUND.getMessage()));
    }

    @Test
    @DisplayName("정보 ID가 숫자가 아니면 잘못된 요청 에러를 응답하고 제출하지 않는다")
    void submitWithInvalidInfoId() throws Exception {
        // given & when
        mockMvc.perform(post(SUBMIT_URL, "invalid-id")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(CONTENT, CONTACT_EMAIL, OBSERVED_AT)))
                .andExpect(status().isBadRequest());

        // then
        then(submitInfoUpdateRequestUsecase).should(never()).submit(any());
    }

    @Test
    @DisplayName("필수 값이 모두 없으면 각 필드의 유효성 검증 에러를 응답하고 제출하지 않는다")
    void submitWithoutRequiredFields() throws Exception {
        // given & when
        mockMvc.perform(post(SUBMIT_URL, INFO_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.errorCode").value(CommonErrorType.VALIDATION_ERROR.getErrorCode()))
                .andExpect(jsonPath("$.errors.content").value("정보 수정 요청 내용은 비어있을 수 없습니다."))
                .andExpect(jsonPath("$.errors.contactEmail").exists())
                .andExpect(jsonPath("$.errors.observedAt").value("수정 정보 확인 시간은 비어있을 수 없습니다."));

        // then
        then(submitInfoUpdateRequestUsecase).should(never()).submit(any());
    }

    @Test
    @DisplayName("요청 내용이 공백이면 유효성 검증 에러를 응답하고 제출하지 않는다")
    void submitWithBlankContent() throws Exception {
        // given & when
        mockMvc.perform(post(SUBMIT_URL, INFO_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("   ", CONTACT_EMAIL, OBSERVED_AT)))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.errorCode").value(CommonErrorType.VALIDATION_ERROR.getErrorCode()))
                .andExpect(jsonPath("$.errors.content").value("정보 수정 요청 내용은 비어있을 수 없습니다."));

        // then
        then(submitInfoUpdateRequestUsecase).should(never()).submit(any());
    }

    @ParameterizedTest
    @ValueSource(strings = {"invalid-email", "user@localhost", "user@yu..ac.kr"})
    @DisplayName("연락용 이메일 형식이 Email VO의 형식과 다르면 유효성 검증 에러를 응답하고 제출하지 않는다")
    void submitWithInvalidContactEmail(String contactEmail) throws Exception {
        // given & when
        mockMvc.perform(post(SUBMIT_URL, INFO_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(CONTENT, contactEmail, OBSERVED_AT)))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.errorCode").value(CommonErrorType.VALIDATION_ERROR.getErrorCode()))
                .andExpect(jsonPath("$.errors.contactEmail").value("이메일 양식이 잘못되었습니다."));

        // then
        then(submitInfoUpdateRequestUsecase).should(never()).submit(any());
    }

    @Test
    @DisplayName("확인 시각 형식이 잘못되면 잘못된 요청 에러를 응답하고 제출하지 않는다")
    void submitWithInvalidObservedAt() throws Exception {
        // given & when
        mockMvc.perform(post(SUBMIT_URL, INFO_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(CONTENT, CONTACT_EMAIL, "2026/10/01 09:00")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value(CommonErrorType.BAD_REQUEST.getErrorCode()));

        // then
        then(submitInfoUpdateRequestUsecase).should(never()).submit(any());
    }

    private String body(String content, String contactEmail, String observedAt) {
        return """
                {"content": "%s", "contactEmail": "%s", "observedAt": "%s"}
                """.formatted(content, contactEmail, observedAt);
    }
}
