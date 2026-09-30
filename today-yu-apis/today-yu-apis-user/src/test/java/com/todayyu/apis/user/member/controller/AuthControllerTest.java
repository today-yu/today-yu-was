package com.todayyu.apis.user.member.controller;

import com.todayyu.apis.common.exception.GlobalExceptionHandler;
import com.todayyu.application.member.dto.VerificationCodeIssueCommand;
import com.todayyu.application.member.dto.VerificationCodeVerifyCommand;
import com.todayyu.application.member.usecase.EmailVerificationUsecase;
import com.todayyu.common.error.BaseErrorType;
import com.todayyu.common.error.ErrorCategory;
import com.todayyu.common.exception.CustomException;
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
class AuthControllerTest {
    private static final String ISSUE_VERIFICATION_CODE_URL = "/api/v1/auth/email-verification/issue";
    private static final String VERIFY_VERIFICATION_CODE_URL = "/api/v1/auth/email-verification/verify";
    private static final String EMAIL = "user@example.com";
    private static final String CODE = "123456";

    @Mock
    private EmailVerificationUsecase emailVerificationUsecase;

    @InjectMocks
    private AuthController authController;

    @Captor
    private ArgumentCaptor<VerificationCodeIssueCommand> commandCaptor;

    @Captor
    private ArgumentCaptor<VerificationCodeVerifyCommand> verifyCommandCaptor;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("이메일 인증 코드 발급 요청 시 요청 이메일로 인증 코드 발급을 요청하고 성공 응답을 반환한다")
    void issueVerificationCode() throws Exception {
        // given & when
        mockMvc.perform(post(ISSUE_VERIFICATION_CODE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "%s"}
                                """.formatted(EMAIL)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("이메일 인증 코드 발급에 성공하였습니다."));

        // then
        then(emailVerificationUsecase).should().issueVerificationCode(commandCaptor.capture());
        assertEquals(EMAIL, commandCaptor.getValue().email().getValue());
    }

    @Test
    @DisplayName("이메일이 없으면 유효성 검증 에러를 응답하고 인증 코드를 발급하지 않는다")
    void issueVerificationCodeWithoutEmail() throws Exception {
        // given & when
        mockMvc.perform(post(ISSUE_VERIFICATION_CODE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.errorCode").value("COMMON-0002"))
                .andExpect(jsonPath("$.errors.email").value("이메일은 비어있을 수 없습니다."));

        // then
        then(emailVerificationUsecase).should(never()).issueVerificationCode(any());
    }

    @ParameterizedTest
    @ValueSource(strings = {"invalid-email", "user@localhost", "user@yu..ac.kr"})
    @DisplayName("이메일 형식이 Email VO의 형식과 다르면 유효성 검증 에러를 응답하고 인증 코드를 발급하지 않는다")
    void issueVerificationCodeWithInvalidEmail(String email) throws Exception {
        // given & when
        mockMvc.perform(post(ISSUE_VERIFICATION_CODE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "%s"}
                                """.formatted(email)))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.errorCode").value("COMMON-0002"))
                .andExpect(jsonPath("$.errors.email").value("이메일 양식이 잘못되었습니다."));

        // then
        then(emailVerificationUsecase).should(never()).issueVerificationCode(any());
    }

    @Test
    @DisplayName("요청 본문을 읽을 수 없으면 잘못된 요청 에러를 응답한다")
    void issueVerificationCodeWithMalformedBody() throws Exception {
        // given & when
        mockMvc.perform(post(ISSUE_VERIFICATION_CODE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": "))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("COMMON-0003"));

        // then
        then(emailVerificationUsecase).should(never()).issueVerificationCode(any());
    }

    @Test
    @DisplayName("인증 코드 발급 중 비즈니스 예외가 발생하면 에러 카테고리에 맞는 상태 코드와 에러 응답을 반환한다")
    void issueVerificationCodeWithBusinessException() throws Exception {
        // given
        willThrow(new CustomException(TestErrorType.TOO_EARLY))
                .given(emailVerificationUsecase).issueVerificationCode(any());

        // when & then
        mockMvc.perform(post(ISSUE_VERIFICATION_CODE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "%s"}
                                """.formatted(EMAIL)))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.errorCode").value(TestErrorType.TOO_EARLY.getErrorCode()))
                .andExpect(jsonPath("$.message").value(TestErrorType.TOO_EARLY.getMessage()));
    }

    @Test
    @DisplayName("이메일 인증 코드 검증 요청 시 요청 이메일과 인증 코드로 검증을 요청하고 성공 응답을 반환한다")
    void verifyVerificationCode() throws Exception {
        // given & when
        mockMvc.perform(post(VERIFY_VERIFICATION_CODE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "%s", "code": "%s"}
                                """.formatted(EMAIL, CODE)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("이메일 인증에 성공하였습니다."));

        // then
        then(emailVerificationUsecase).should().verify(verifyCommandCaptor.capture());
        assertEquals(EMAIL, verifyCommandCaptor.getValue().email().getValue());
        assertEquals(CODE, verifyCommandCaptor.getValue().code().getValue());
    }

    @Test
    @DisplayName("검증 요청에 이메일이 없으면 유효성 검증 에러를 응답하고 인증 코드를 검증하지 않는다")
    void verifyVerificationCodeWithoutEmail() throws Exception {
        // given & when
        mockMvc.perform(post(VERIFY_VERIFICATION_CODE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"code": "%s"}
                                """.formatted(CODE)))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.errorCode").value("COMMON-0002"))
                .andExpect(jsonPath("$.errors.email").value("이메일은 비어있을 수 없습니다."));

        // then
        then(emailVerificationUsecase).should(never()).verify(any());
    }

    @ParameterizedTest
    @ValueSource(strings = {"invalid-email", "user@localhost", "user@yu..ac.kr"})
    @DisplayName("검증 요청의 이메일 형식이 Email VO의 형식과 다르면 유효성 검증 에러를 응답하고 인증 코드를 검증하지 않는다")
    void verifyVerificationCodeWithInvalidEmail(String email) throws Exception {
        // given & when
        mockMvc.perform(post(VERIFY_VERIFICATION_CODE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "%s", "code": "%s"}
                                """.formatted(email, CODE)))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.errorCode").value("COMMON-0002"))
                .andExpect(jsonPath("$.errors.email").value("이메일 양식이 잘못되었습니다."));

        // then
        then(emailVerificationUsecase).should(never()).verify(any());
    }

    @Test
    @DisplayName("검증 요청에 인증 코드가 없으면 유효성 검증 에러를 응답하고 인증 코드를 검증하지 않는다")
    void verifyVerificationCodeWithoutCode() throws Exception {
        // given & when
        mockMvc.perform(post(VERIFY_VERIFICATION_CODE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "%s"}
                                """.formatted(EMAIL)))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.errorCode").value("COMMON-0002"))
                .andExpect(jsonPath("$.errors.code").value("인증 코드는 비어있을 수 없습니다."));

        // then
        then(emailVerificationUsecase).should(never()).verify(any());
    }

    @Test
    @DisplayName("인증 코드 검증에 실패하면 에러 카테고리에 맞는 상태 코드와 에러 응답을 반환한다")
    void verifyVerificationCodeWithBusinessException() throws Exception {
        // given
        willThrow(new CustomException(TestErrorType.VERIFICATION_FAIL))
                .given(emailVerificationUsecase).verify(any());

        // when & then
        mockMvc.perform(post(VERIFY_VERIFICATION_CODE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "%s", "code": "%s"}
                                """.formatted(EMAIL, CODE)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value(TestErrorType.VERIFICATION_FAIL.getErrorCode()))
                .andExpect(jsonPath("$.message").value(TestErrorType.VERIFICATION_FAIL.getMessage()));
    }

    /**
     * 컨트롤러 테스트가 도메인 에러 타입 정의에 영향받지 않도록, 도메인 에러 타입 대신 사용하는 테스트용 에러 타입
     */
    private enum TestErrorType implements BaseErrorType {
        TOO_EARLY(ErrorCategory.TOO_MANY_REQUESTS, "TEST-0001", "재발급 제한 시간 내 요청입니다."),
        VERIFICATION_FAIL(ErrorCategory.BUSINESS, "TEST-0002", "인증 코드 검증에 실패하였습니다.");

        private final ErrorCategory category;
        private final String errorCode;
        private final String message;

        TestErrorType(ErrorCategory category, String errorCode, String message) {
            this.category = category;
            this.errorCode = errorCode;
            this.message = message;
        }

        @Override
        public ErrorCategory getCategory() {
            return category;
        }

        @Override
        public String getErrorCode() {
            return errorCode;
        }

        @Override
        public String getMessage() {
            return message;
        }
    }
}
