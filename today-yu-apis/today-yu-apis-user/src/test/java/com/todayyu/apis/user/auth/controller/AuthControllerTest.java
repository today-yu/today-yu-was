package com.todayyu.apis.user.auth.controller;

import com.todayyu.apis.common.auth.AuthScheme;
import com.todayyu.apis.common.exception.GlobalExceptionHandler;
import com.todayyu.application.auth.dto.SigninCommand;
import com.todayyu.application.auth.dto.SigninResult;
import com.todayyu.application.auth.error.BusinessAuthErrorType;
import com.todayyu.application.auth.usecase.SigninUsecase;
import com.todayyu.application.auth.usecase.SignoutUsecase;
import com.todayyu.common.error.BaseErrorType;
import com.todayyu.common.error.ErrorCategory;
import com.todayyu.common.exception.CustomException;
import com.todayyu.domain.auth.vo.AccessToken;
import com.todayyu.domain.auth.vo.RefreshToken;
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
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {
    private static final String SIGNIN_URL = "/api/v1/auth/sign-in";
    private static final String SIGNOUT_URL = "/api/v1/auth/sign-out";
    private static final String EMAIL = "user@example.com";
    private static final String PASSWORD = "password1234";
    private static final String INVALID_PASSWORD_LENGTH_MESSAGE =
            "비밀번호는 최소 " + Password.MIN_LENGTH + "자 이상, 최대 " + Password.MAX_LENGTH + "자 이하여야 합니다.";
    private static final String ACCESS_TOKEN = "access-token";
    private static final String REFRESH_TOKEN = "refresh-token";
    private static final Instant ACCESS_TOKEN_EXPIRES_AT = Instant.parse("2026-10-04T00:30:00Z");
    private static final Instant REFRESH_TOKEN_EXPIRES_AT = Instant.parse("2026-10-18T00:00:00Z");

    @Mock
    private SigninUsecase signinUsecase;

    @Mock
    private SignoutUsecase signoutUsecase;

    @InjectMocks
    private AuthController authController;

    @Captor
    private ArgumentCaptor<SigninCommand> signinCommandCaptor;

    @Captor
    private ArgumentCaptor<AccessToken> accessTokenCaptor;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("로그인 요청 시 요청 이메일과 비밀번호로 로그인을 요청하고, 캐시되지 않는 응답으로 토큰과 만료 시각을 반환한다")
    void signin() throws Exception {
        // given
        given(signinUsecase.signin(any())).willReturn(new SigninResult(
                new AccessToken(ACCESS_TOKEN), ACCESS_TOKEN_EXPIRES_AT,
                new RefreshToken(REFRESH_TOKEN), REFRESH_TOKEN_EXPIRES_AT
        ));

        // when
        mockMvc.perform(post(SIGNIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "%s", "password": "%s"}
                                """.formatted(EMAIL, PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(header().string("Cache-Control", "no-store"))
                .andExpect(jsonPath("$.message").value("로그인에 성공하였습니다."))
                .andExpect(jsonPath("$.data.tokenType").value(AuthScheme.BEARER))
                .andExpect(jsonPath("$.data.accessToken").value(ACCESS_TOKEN))
                .andExpect(jsonPath("$.data.accessTokenExpiresAt").value(ACCESS_TOKEN_EXPIRES_AT.toString()))
                .andExpect(jsonPath("$.data.refreshToken").value(REFRESH_TOKEN))
                .andExpect(jsonPath("$.data.refreshTokenExpiresAt").value(REFRESH_TOKEN_EXPIRES_AT.toString()));

        // then
        then(signinUsecase).should().signin(signinCommandCaptor.capture());
        assertEquals(EMAIL, signinCommandCaptor.getValue().email().getValue());
        assertEquals(PASSWORD, signinCommandCaptor.getValue().password().getValue());
    }

    @Test
    @DisplayName("로그인 요청에 이메일이 없으면 유효성 검증 에러를 응답하고 로그인을 하지 않는다")
    void signinWithoutEmail() throws Exception {
        // given & when
        mockMvc.perform(post(SIGNIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"password": "%s"}
                                """.formatted(PASSWORD)))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.errorCode").value("COMMON-0002"))
                .andExpect(jsonPath("$.errors.email").value("이메일은 비어있을 수 없습니다."));

        // then
        then(signinUsecase).should(never()).signin(any());
    }

    @ParameterizedTest
    @ValueSource(strings = {"invalid-email", "user@localhost", "user@yu..ac.kr"})
    @DisplayName("로그인 요청의 이메일 형식이 Email VO의 형식과 다르면 유효성 검증 에러를 응답하고 로그인을 하지 않는다")
    void signinWithInvalidEmail(String email) throws Exception {
        // given & when
        mockMvc.perform(post(SIGNIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "%s", "password": "%s"}
                                """.formatted(email, PASSWORD)))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.errorCode").value("COMMON-0002"))
                .andExpect(jsonPath("$.errors.email").value("이메일 양식이 잘못되었습니다."));

        // then
        then(signinUsecase).should(never()).signin(any());
    }

    @Test
    @DisplayName("로그인 요청에 비밀번호가 없으면 유효성 검증 에러를 응답하고 로그인을 하지 않는다")
    void signinWithoutPassword() throws Exception {
        // given & when
        mockMvc.perform(post(SIGNIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "%s"}
                                """.formatted(EMAIL)))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.errorCode").value("COMMON-0002"))
                .andExpect(jsonPath("$.errors.password").value("비밀번호는 비어있을 수 없습니다."));

        // then
        then(signinUsecase).should(never()).signin(any());
    }

    @ParameterizedTest
    @ValueSource(ints = {Password.MIN_LENGTH - 1, Password.MAX_LENGTH + 1})
    @DisplayName("로그인 요청의 비밀번호 길이가 허용 범위를 벗어나면 유효성 검증 에러를 응답하고 로그인을 하지 않는다")
    void signinWithInvalidPasswordLength(int passwordLength) throws Exception {
        // given & when
        mockMvc.perform(post(SIGNIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "%s", "password": "%s"}
                                """.formatted(EMAIL, "a".repeat(passwordLength))))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.errorCode").value("COMMON-0002"))
                .andExpect(jsonPath("$.errors.password").value(INVALID_PASSWORD_LENGTH_MESSAGE));

        // then
        then(signinUsecase).should(never()).signin(any());
    }

    @Test
    @DisplayName("로그인 중 비즈니스 예외가 발생하면 에러 카테고리에 맞는 상태 코드와 에러 응답을 반환한다")
    void signinWithBusinessException() throws Exception {
        // given
        willThrow(new CustomException(TestErrorType.UNAUTHORIZED))
                .given(signinUsecase).signin(any());

        // when & then
        mockMvc.perform(post(SIGNIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "%s", "password": "%s"}
                                """.formatted(EMAIL, PASSWORD)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value(TestErrorType.UNAUTHORIZED.getErrorCode()))
                .andExpect(jsonPath("$.message").value(TestErrorType.UNAUTHORIZED.getMessage()));
    }

    @Test
    @DisplayName("로그아웃 요청 시 Authorization 헤더의 Access Token으로 로그아웃을 요청하고 성공 응답을 반환한다")
    void signout() throws Exception {
        // given & when
        mockMvc.perform(post(SIGNOUT_URL)
                        .header(HttpHeaders.AUTHORIZATION, AuthScheme.BEARER + " " + ACCESS_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("로그아웃에 성공하였습니다."));

        // then
        then(signoutUsecase).should().signout(accessTokenCaptor.capture());
        assertEquals(ACCESS_TOKEN, accessTokenCaptor.getValue().getValue());
    }

    @Test
    @DisplayName("로그아웃 요청에 Authorization 헤더가 없으면 잘못된 요청 에러를 응답하고 로그아웃을 하지 않는다")
    void signoutWithoutAuthorizationHeader() throws Exception {
        // given & when
        mockMvc.perform(post(SIGNOUT_URL))
                .andExpect(status().isBadRequest());

        // then
        then(signoutUsecase).should(never()).signout(any());
    }

    @Test
    @DisplayName("로그아웃 요청의 Access Token이 비어있으면 유효하지 않은 토큰 에러를 응답하고 로그아웃을 하지 않는다")
    void signoutWithBlankAccessToken() throws Exception {
        // given & when
        mockMvc.perform(post(SIGNOUT_URL)
                        .header(HttpHeaders.AUTHORIZATION, AuthScheme.BEARER + " "))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value(BusinessAuthErrorType.INVALID_TOKEN.getErrorCode()));

        // then
        then(signoutUsecase).should(never()).signout(any());
    }

    @Test
    @DisplayName("로그아웃 중 비즈니스 예외가 발생하면 에러 카테고리에 맞는 상태 코드와 에러 응답을 반환한다")
    void signoutWithBusinessException() throws Exception {
        // given
        willThrow(new CustomException(TestErrorType.UNAUTHORIZED))
                .given(signoutUsecase).signout(any());

        // when & then
        mockMvc.perform(post(SIGNOUT_URL)
                        .header(HttpHeaders.AUTHORIZATION, AuthScheme.BEARER + " " + ACCESS_TOKEN))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value(TestErrorType.UNAUTHORIZED.getErrorCode()))
                .andExpect(jsonPath("$.message").value(TestErrorType.UNAUTHORIZED.getMessage()));
    }

    /**
     * 컨트롤러 테스트가 도메인 에러 타입 정의에 영향받지 않도록, 도메인 에러 타입 대신 사용하는 테스트용 에러 타입
     */
    private enum TestErrorType implements BaseErrorType {
        UNAUTHORIZED(ErrorCategory.UNAUTHORIZED, "TEST-0001", "인증에 실패하였습니다.");

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
