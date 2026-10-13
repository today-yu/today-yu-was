package com.todayyu.apis.user.info.controller;

import com.todayyu.apis.common.exception.GlobalExceptionHandler;
import com.todayyu.apis.common.security.principal.CustomUserDetails;
import com.todayyu.application.info.dto.SubmitInfoRegistrationRequestCommand;
import com.todayyu.application.info.dto.SubmitInfoRegistrationRequestResult;
import com.todayyu.application.info.usecase.SubmitInfoRegistrationRequestUsecase;
import com.todayyu.common.error.CommonErrorType;
import com.todayyu.domain.common.vo.Email;
import com.todayyu.domain.info.vo.InfoCategory;
import com.todayyu.domain.info.vo.InfoRegistrationRequestContent;
import com.todayyu.domain.info.vo.InfoRegistrationRequestId;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class InfoRegistrationRequestControllerTest {
    private static final String SUBMIT_URL = "/api/v1/infos/registration-requests";
    private static final Long MEMBER_ID = 1L;
    private static final Long REQUEST_ID = 10L;
    private static final String CATEGORY = "ACADEMIC";
    private static final String CONTENT = "수강신청 일정을 등록해주세요.";
    private static final String CONTACT_EMAIL = "requester@example.com";

    @Mock
    private SubmitInfoRegistrationRequestUsecase submitInfoRegistrationRequestUsecase;

    @InjectMocks
    private InfoRegistrationRequestController infoRegistrationRequestController;

    @Captor
    private ArgumentCaptor<SubmitInfoRegistrationRequestCommand> commandCaptor;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(infoRegistrationRequestController)
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
    @DisplayName("정보 등록 요청 제출 시 인증된 회원 ID와 요청 본문으로 제출을 요청하고, 생성된 정보 등록 요청 ID를 반환한다")
    void submit() throws Exception {
        // given
        given(submitInfoRegistrationRequestUsecase.submit(any()))
                .willReturn(new SubmitInfoRegistrationRequestResult(new InfoRegistrationRequestId(REQUEST_ID)));

        // when
        mockMvc.perform(post(SUBMIT_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(CATEGORY, CONTENT, CONTACT_EMAIL)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("정보 등록 요청 제출에 성공하였습니다."))
                .andExpect(jsonPath("$.data.infoRegistrationRequestId").value(REQUEST_ID));

        // then
        then(submitInfoRegistrationRequestUsecase).should().submit(commandCaptor.capture());
        SubmitInfoRegistrationRequestCommand command = commandCaptor.getValue();
        assertEquals(new MemberId(MEMBER_ID), command.requesterId());
        assertEquals(InfoCategory.ACADEMIC, command.category());
        assertEquals(new InfoRegistrationRequestContent(CONTENT), command.content());
        assertEquals(new Email(CONTACT_EMAIL), command.contactEmail());
    }

    @Test
    @DisplayName("요청 본문에 요청자 ID가 포함되어도 무시하고 인증된 회원 ID로 제출을 요청한다")
    void submitIgnoresRequesterIdInBody() throws Exception {
        // given
        given(submitInfoRegistrationRequestUsecase.submit(any()))
                .willReturn(new SubmitInfoRegistrationRequestResult(new InfoRegistrationRequestId(REQUEST_ID)));

        // when
        mockMvc.perform(post(SUBMIT_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"requesterId": 999, "category": "%s", "content": "%s", "contactEmail": "%s"}
                                """.formatted(CATEGORY, CONTENT, CONTACT_EMAIL)))
                .andExpect(status().isCreated());

        // then
        then(submitInfoRegistrationRequestUsecase).should().submit(commandCaptor.capture());
        assertEquals(new MemberId(MEMBER_ID), commandCaptor.getValue().requesterId());
    }

    @Test
    @DisplayName("필수 값이 모두 없으면 각 필드의 유효성 검증 에러를 응답하고 제출하지 않는다")
    void submitWithoutRequiredFields() throws Exception {
        // given & when
        mockMvc.perform(post(SUBMIT_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.errorCode").value(CommonErrorType.VALIDATION_ERROR.getErrorCode()))
                .andExpect(jsonPath("$.errors.category").value("정보 카테고리는 비어있을 수 없습니다."))
                .andExpect(jsonPath("$.errors.content").value("정보 등록 요청 내용은 비어있을 수 없습니다."))
                .andExpect(jsonPath("$.errors.contactEmail").exists());

        // then
        then(submitInfoRegistrationRequestUsecase).should(never()).submit(any());
    }

    @Test
    @DisplayName("요청 내용이 공백이면 유효성 검증 에러를 응답하고 제출하지 않는다")
    void submitWithBlankContent() throws Exception {
        // given & when
        mockMvc.perform(post(SUBMIT_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(CATEGORY, "   ", CONTACT_EMAIL)))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.errorCode").value(CommonErrorType.VALIDATION_ERROR.getErrorCode()))
                .andExpect(jsonPath("$.errors.content").value("정보 등록 요청 내용은 비어있을 수 없습니다."));

        // then
        then(submitInfoRegistrationRequestUsecase).should(never()).submit(any());
    }

    @ParameterizedTest
    @ValueSource(strings = {"invalid-email", "user@localhost", "user@yu..ac.kr"})
    @DisplayName("연락용 이메일 형식이 Email VO의 형식과 다르면 유효성 검증 에러를 응답하고 제출하지 않는다")
    void submitWithInvalidContactEmail(String contactEmail) throws Exception {
        // given & when
        mockMvc.perform(post(SUBMIT_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(CATEGORY, CONTENT, contactEmail)))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.errorCode").value(CommonErrorType.VALIDATION_ERROR.getErrorCode()))
                .andExpect(jsonPath("$.errors.contactEmail").value("이메일 양식이 잘못되었습니다."));

        // then
        then(submitInfoRegistrationRequestUsecase).should(never()).submit(any());
    }

    @Test
    @DisplayName("존재하지 않는 카테고리로 요청하면 잘못된 요청 에러를 응답하고 제출하지 않는다")
    void submitWithInvalidCategory() throws Exception {
        // given & when
        mockMvc.perform(post(SUBMIT_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("INVALID", CONTENT, CONTACT_EMAIL)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value(CommonErrorType.BAD_REQUEST.getErrorCode()));

        // then
        then(submitInfoRegistrationRequestUsecase).should(never()).submit(any());
    }

    private String body(String category, String content, String contactEmail) {
        return """
                {"category": "%s", "content": "%s", "contactEmail": "%s"}
                """.formatted(category, content, contactEmail);
    }
}
