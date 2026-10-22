package com.todayyu.apis.user.member.controller;

import com.todayyu.apis.common.exception.GlobalExceptionHandler;
import com.todayyu.application.member.dto.command.IssuePasswordResetVerificationCommand;
import com.todayyu.application.member.usecase.IssuePasswordResetVerificationUsecase;
import com.todayyu.common.exception.CustomException;
import com.todayyu.domain.member.error.MemberErrorType;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class PasswordControllerTest {
    private static final String ISSUE_PASSWORD_RESET_VERIFICATION_URL = "/api/v1/auth/password-reset-verification/issue";
    private static final String EMAIL = "user@example.com";

    @Mock
    private IssuePasswordResetVerificationUsecase issuePasswordResetVerificationUsecase;

    @InjectMocks
    private PasswordController passwordController;

    @Captor
    private ArgumentCaptor<IssuePasswordResetVerificationCommand> commandCaptor;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(passwordController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("비밀번호 재설정 인증 코드 발급 요청 시 요청 이메일로 발급을 요청하고 성공 응답을 반환한다")
    void issuePasswordResetVerification() throws Exception {
        // given & when
        mockMvc.perform(post(ISSUE_PASSWORD_RESET_VERIFICATION_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "%s"}
                                """.formatted(EMAIL)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("비밀번호 초기화 인증 코드 이메일 발송 요청에 성공하였습니다."));

        // then
        then(issuePasswordResetVerificationUsecase).should().issue(commandCaptor.capture());
        assertEquals(EMAIL, commandCaptor.getValue().email().getValue());
    }

    @Test
    @DisplayName("이메일이 없으면 유효성 검증 에러를 응답하고 인증 코드를 발급하지 않는다")
    void issuePasswordResetVerificationWithoutEmail() throws Exception {
        // given & when
        mockMvc.perform(post(ISSUE_PASSWORD_RESET_VERIFICATION_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.errorCode").value("COMMON-0002"))
                .andExpect(jsonPath("$.errors.email").value("이메일은 비어있을 수 없습니다."));

        // then
        then(issuePasswordResetVerificationUsecase).should(never()).issue(any());
    }

    @ParameterizedTest
    @ValueSource(strings = {"invalid-email", "user@localhost", "user@yu..ac.kr"})
    @DisplayName("이메일 형식이 Email VO의 형식과 다르면 유효성 검증 에러를 응답하고 인증 코드를 발급하지 않는다")
    void issuePasswordResetVerificationWithInvalidEmail(String email) throws Exception {
        // given & when
        mockMvc.perform(post(ISSUE_PASSWORD_RESET_VERIFICATION_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "%s"}
                                """.formatted(email)))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.errorCode").value("COMMON-0002"))
                .andExpect(jsonPath("$.errors.email").value("이메일 양식이 잘못되었습니다."));

        // then
        then(issuePasswordResetVerificationUsecase).should(never()).issue(any());
    }

    @Test
    @DisplayName("요청 본문을 읽을 수 없으면 잘못된 요청 에러를 응답한다")
    void issuePasswordResetVerificationWithMalformedBody() throws Exception {
        // given & when
        mockMvc.perform(post(ISSUE_PASSWORD_RESET_VERIFICATION_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": "))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("COMMON-0003"));

        // then
        then(issuePasswordResetVerificationUsecase).should(never()).issue(any());
    }

    @Test
    @DisplayName("발급 후 1분 이내 재발급을 요청하면 429 에러를 응답한다")
    void issuePasswordResetVerificationTooEarly() throws Exception {
        // given
        willThrow(new CustomException(MemberErrorType.VERIFICATION_CODE_REISSUE_TOO_EARLY))
                .given(issuePasswordResetVerificationUsecase).issue(any());

        // when & then
        mockMvc.perform(post(ISSUE_PASSWORD_RESET_VERIFICATION_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "%s"}
                                """.formatted(EMAIL)))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.errorCode").value(MemberErrorType.VERIFICATION_CODE_REISSUE_TOO_EARLY.getErrorCode()))
                .andExpect(jsonPath("$.message").value(MemberErrorType.VERIFICATION_CODE_REISSUE_TOO_EARLY.getMessage()));
    }
}
