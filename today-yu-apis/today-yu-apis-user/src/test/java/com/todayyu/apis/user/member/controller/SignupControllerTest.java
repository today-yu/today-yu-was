package com.todayyu.apis.user.member.controller;

import com.todayyu.apis.common.exception.GlobalExceptionHandler;
import com.todayyu.application.member.dto.command.SignupCommand;
import com.todayyu.application.member.dto.command.IssueEmailVerificationCommand;
import com.todayyu.application.member.dto.command.VerifyEmailVerificationCommand;
import com.todayyu.application.member.usecase.IssueEmailVerificationUsecase;
import com.todayyu.application.member.usecase.SignupUsecase;
import com.todayyu.application.member.usecase.VerifyEmailVerificationUsecase;
import com.todayyu.common.error.BaseErrorType;
import com.todayyu.common.error.ErrorCategory;
import com.todayyu.common.exception.CustomException;
import com.todayyu.domain.member.vo.Password;
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
class SignupControllerTest {
    private static final String ISSUE_EMAIL_VERIFICATION_URL = "/api/v1/auth/email-verification/issue";
    private static final String VERIFY_EMAIL_VERIFICATION_URL = "/api/v1/auth/email-verification/verify";
    private static final String EMAIL = "user@example.com";
    private static final String SIGNUP_URL = "/api/v1/auth/sign-up";
    private static final String CODE = "123456";
    private static final String PASSWORD = "password1234";
    private static final String INVALID_PASSWORD_LENGTH_MESSAGE =
            "비밀번호는 최소 " + Password.MIN_LENGTH + "자 이상, 최대 " + Password.MAX_LENGTH + "자 이하여야 합니다.";

    @Mock
    private IssueEmailVerificationUsecase issueEmailVerificationUsecase;

    @Mock
    private VerifyEmailVerificationUsecase verifyEmailVerificationUsecase;

    @Mock
    private SignupUsecase signupUsecase;

    @InjectMocks
    private SignupController signupController;

    @Captor
    private ArgumentCaptor<IssueEmailVerificationCommand> commandCaptor;

    @Captor
    private ArgumentCaptor<VerifyEmailVerificationCommand> verifyCommandCaptor;

    @Captor
    private ArgumentCaptor<SignupCommand> signupCommandCaptor;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(signupController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("이메일 인증 코드 발급 요청 시 요청 이메일로 인증 코드 발급을 요청하고 성공 응답을 반환한다")
    void issueEmailVerification() throws Exception {
        // given & when
        mockMvc.perform(post(ISSUE_EMAIL_VERIFICATION_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "%s"}
                                """.formatted(EMAIL)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("이메일 인증 코드 발급에 성공하였습니다."));

        // then
        then(issueEmailVerificationUsecase).should().issue(commandCaptor.capture());
        assertEquals(EMAIL, commandCaptor.getValue().email().getValue());
    }

    @Test
    @DisplayName("이메일이 없으면 유효성 검증 에러를 응답하고 인증 코드를 발급하지 않는다")
    void issueEmailVerificationWithoutEmail() throws Exception {
        // given & when
        mockMvc.perform(post(ISSUE_EMAIL_VERIFICATION_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.errorCode").value("COMMON-0002"))
                .andExpect(jsonPath("$.errors.email").value("이메일은 비어있을 수 없습니다."));

        // then
        then(issueEmailVerificationUsecase).should(never()).issue(any());
    }

    @ParameterizedTest
    @ValueSource(strings = {"invalid-email", "user@localhost", "user@yu..ac.kr"})
    @DisplayName("이메일 형식이 Email VO의 형식과 다르면 유효성 검증 에러를 응답하고 인증 코드를 발급하지 않는다")
    void issueEmailVerificationWithInvalidEmail(String email) throws Exception {
        // given & when
        mockMvc.perform(post(ISSUE_EMAIL_VERIFICATION_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "%s"}
                                """.formatted(email)))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.errorCode").value("COMMON-0002"))
                .andExpect(jsonPath("$.errors.email").value("이메일 양식이 잘못되었습니다."));

        // then
        then(issueEmailVerificationUsecase).should(never()).issue(any());
    }

    @Test
    @DisplayName("요청 본문을 읽을 수 없으면 잘못된 요청 에러를 응답한다")
    void issueEmailVerificationWithMalformedBody() throws Exception {
        // given & when
        mockMvc.perform(post(ISSUE_EMAIL_VERIFICATION_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": "))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("COMMON-0003"));

        // then
        then(issueEmailVerificationUsecase).should(never()).issue(any());
    }

    @Test
    @DisplayName("인증 코드 발급 중 비즈니스 예외가 발생하면 에러 카테고리에 맞는 상태 코드와 에러 응답을 반환한다")
    void issueEmailVerificationWithBusinessException() throws Exception {
        // given
        willThrow(new CustomException(TestErrorType.TOO_EARLY))
                .given(issueEmailVerificationUsecase).issue(any());

        // when & then
        mockMvc.perform(post(ISSUE_EMAIL_VERIFICATION_URL)
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
    void verifyEmailVerification() throws Exception {
        // given & when
        mockMvc.perform(post(VERIFY_EMAIL_VERIFICATION_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "%s", "code": "%s"}
                                """.formatted(EMAIL, CODE)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("이메일 인증에 성공하였습니다."));

        // then
        then(verifyEmailVerificationUsecase).should().verify(verifyCommandCaptor.capture());
        assertEquals(EMAIL, verifyCommandCaptor.getValue().email().getValue());
        assertEquals(CODE, verifyCommandCaptor.getValue().code().getValue());
    }

    @Test
    @DisplayName("검증 요청에 이메일이 없으면 유효성 검증 에러를 응답하고 인증 코드를 검증하지 않는다")
    void verifyEmailVerificationWithoutEmail() throws Exception {
        // given & when
        mockMvc.perform(post(VERIFY_EMAIL_VERIFICATION_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"code": "%s"}
                                """.formatted(CODE)))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.errorCode").value("COMMON-0002"))
                .andExpect(jsonPath("$.errors.email").value("이메일은 비어있을 수 없습니다."));

        // then
        then(verifyEmailVerificationUsecase).should(never()).verify(any());
    }

    @ParameterizedTest
    @ValueSource(strings = {"invalid-email", "user@localhost", "user@yu..ac.kr"})
    @DisplayName("검증 요청의 이메일 형식이 Email VO의 형식과 다르면 유효성 검증 에러를 응답하고 인증 코드를 검증하지 않는다")
    void verifyEmailVerificationWithInvalidEmail(String email) throws Exception {
        // given & when
        mockMvc.perform(post(VERIFY_EMAIL_VERIFICATION_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "%s", "code": "%s"}
                                """.formatted(email, CODE)))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.errorCode").value("COMMON-0002"))
                .andExpect(jsonPath("$.errors.email").value("이메일 양식이 잘못되었습니다."));

        // then
        then(verifyEmailVerificationUsecase).should(never()).verify(any());
    }

    @Test
    @DisplayName("검증 요청에 인증 코드가 없으면 유효성 검증 에러를 응답하고 인증 코드를 검증하지 않는다")
    void verifyEmailVerificationWithoutCode() throws Exception {
        // given & when
        mockMvc.perform(post(VERIFY_EMAIL_VERIFICATION_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "%s"}
                                """.formatted(EMAIL)))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.errorCode").value("COMMON-0002"))
                .andExpect(jsonPath("$.errors.code").value("인증 코드는 비어있을 수 없습니다."));

        // then
        then(verifyEmailVerificationUsecase).should(never()).verify(any());
    }

    @Test
    @DisplayName("인증 코드 검증에 실패하면 에러 카테고리에 맞는 상태 코드와 에러 응답을 반환한다")
    void verifyEmailVerificationWithBusinessException() throws Exception {
        // given
        willThrow(new CustomException(TestErrorType.VERIFICATION_FAIL))
                .given(verifyEmailVerificationUsecase).verify(any());

        // when & then
        mockMvc.perform(post(VERIFY_EMAIL_VERIFICATION_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "%s", "code": "%s"}
                                """.formatted(EMAIL, CODE)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value(TestErrorType.VERIFICATION_FAIL.getErrorCode()))
                .andExpect(jsonPath("$.message").value(TestErrorType.VERIFICATION_FAIL.getMessage()));
    }

    @ParameterizedTest
    @ValueSource(ints = {Password.MIN_LENGTH, Password.MAX_LENGTH})
    @DisplayName("회원가입 요청 시 요청 이메일과 비밀번호로 회원가입을 요청하고 생성 응답을 반환한다")
    void signup(int passwordLength) throws Exception {
        // given
        String password = "a".repeat(passwordLength);

        // when
        mockMvc.perform(post(SIGNUP_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "%s", "password": "%s"}
                                """.formatted(EMAIL, password)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("회원가입에 성공하였습니다."));

        // then
        then(signupUsecase).should().signup(signupCommandCaptor.capture());
        assertEquals(EMAIL, signupCommandCaptor.getValue().email().getValue());
        assertEquals(password, signupCommandCaptor.getValue().password().getValue());
    }

    @Test
    @DisplayName("회원가입 요청에 이메일이 없으면 유효성 검증 에러를 응답하고 회원가입을 하지 않는다")
    void signupWithoutEmail() throws Exception {
        // given & when
        mockMvc.perform(post(SIGNUP_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"password": "%s"}
                                """.formatted(PASSWORD)))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.errorCode").value("COMMON-0002"))
                .andExpect(jsonPath("$.errors.email").value("이메일은 비어있을 수 없습니다."));

        // then
        then(signupUsecase).should(never()).signup(any());
    }

    @ParameterizedTest
    @ValueSource(strings = {"invalid-email", "user@localhost", "user@yu..ac.kr"})
    @DisplayName("회원가입 요청의 이메일 형식이 Email VO의 형식과 다르면 유효성 검증 에러를 응답하고 회원가입을 하지 않는다")
    void signupWithInvalidEmail(String email) throws Exception {
        // given & when
        mockMvc.perform(post(SIGNUP_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "%s", "password": "%s"}
                                """.formatted(email, PASSWORD)))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.errorCode").value("COMMON-0002"))
                .andExpect(jsonPath("$.errors.email").value("이메일 양식이 잘못되었습니다."));

        // then
        then(signupUsecase).should(never()).signup(any());
    }

    @Test
    @DisplayName("회원가입 요청에 비밀번호가 없으면 유효성 검증 에러를 응답하고 회원가입을 하지 않는다")
    void signupWithoutPassword() throws Exception {
        // given & when
        mockMvc.perform(post(SIGNUP_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "%s"}
                                """.formatted(EMAIL)))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.errorCode").value("COMMON-0002"))
                .andExpect(jsonPath("$.errors.password").value("비밀번호는 비어있을 수 없습니다."));

        // then
        then(signupUsecase).should(never()).signup(any());
    }

    @ParameterizedTest
    @ValueSource(ints = {Password.MIN_LENGTH - 1, Password.MAX_LENGTH + 1})
    @DisplayName("회원가입 요청의 비밀번호 길이가 허용 범위를 벗어나면 유효성 검증 에러를 응답하고 회원가입을 하지 않는다")
    void signupWithInvalidPasswordLength(int passwordLength) throws Exception {
        // given & when
        mockMvc.perform(post(SIGNUP_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "%s", "password": "%s"}
                                """.formatted(EMAIL, "a".repeat(passwordLength))))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.errorCode").value("COMMON-0002"))
                .andExpect(jsonPath("$.errors.password").value(INVALID_PASSWORD_LENGTH_MESSAGE));

        // then
        then(signupUsecase).should(never()).signup(any());
    }

    @Test
    @DisplayName("회원가입 중 비즈니스 예외가 발생하면 에러 카테고리에 맞는 상태 코드와 에러 응답을 반환한다")
    void signupWithBusinessException() throws Exception {
        // given
        willThrow(new CustomException(TestErrorType.DUPLICATED))
                .given(signupUsecase).signup(any());

        // when & then
        mockMvc.perform(post(SIGNUP_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "%s", "password": "%s"}
                                """.formatted(EMAIL, PASSWORD)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value(TestErrorType.DUPLICATED.getErrorCode()))
                .andExpect(jsonPath("$.message").value(TestErrorType.DUPLICATED.getMessage()));
    }

    /**
     * 컨트롤러 테스트가 도메인 에러 타입 정의에 영향받지 않도록, 도메인 에러 타입 대신 사용하는 테스트용 에러 타입
     */
    private enum TestErrorType implements BaseErrorType {
        TOO_EARLY(ErrorCategory.TOO_MANY_REQUESTS, "TEST-0001", "재발급 제한 시간 내 요청입니다."),
        VERIFICATION_FAIL(ErrorCategory.BUSINESS, "TEST-0002", "인증 코드 검증에 실패하였습니다."),
        DUPLICATED(ErrorCategory.CONFLICT, "TEST-0003", "이미 존재하는 리소스입니다.");

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
