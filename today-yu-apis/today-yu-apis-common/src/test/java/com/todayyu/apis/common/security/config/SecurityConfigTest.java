package com.todayyu.apis.common.security.config;

import com.todayyu.apis.common.security.handler.SecurityErrorResponseWriter;
import com.todayyu.apis.common.security.principal.CustomUserDetails;
import com.todayyu.application.auth.dto.AuthenticatedMember;
import com.todayyu.application.auth.error.BusinessAuthErrorType;
import com.todayyu.application.auth.usecase.AuthenticationUsecase;
import com.todayyu.common.error.CommonErrorType;
import com.todayyu.common.exception.CustomException;
import com.todayyu.domain.member.vo.MemberId;
import com.todayyu.domain.member.vo.Role;
import jakarta.servlet.Filter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import tools.jackson.databind.json.JsonMapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * {@link SecurityConfig}의 필터 체인에 JWT 인증 필터와 공개 엔드포인트가 올바르게 적용되는지 검증한다.
 */
@SpringJUnitWebConfig(SecurityConfigTest.TestConfig.class)
class SecurityConfigTest {
    private static final String PROTECTED_URI = "/api/v1/members/me";
    private static final String BEARER_TOKEN = "Bearer access-token";

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private AuthenticationUsecase authenticationUsecase;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        Mockito.reset(authenticationUsecase);
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .addFilters(context.getBean("springSecurityFilterChain", Filter.class))
                .build();
    }

    @Test
    @DisplayName("보호된 엔드포인트에 유효한 토큰으로 요청하면 인증된 회원 정보로 요청이 처리된다")
    void protectedEndpointWithValidToken() throws Exception {
        // given
        given(authenticationUsecase.authenticate(any()))
                .willReturn(new AuthenticatedMember(new MemberId(1L), Role.MEMBER));

        // when & then
        mockMvc.perform(get(PROTECTED_URI).header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN))
                .andExpect(status().isOk())
                .andExpect(content().string("1"));
    }

    @Test
    @DisplayName("보호된 엔드포인트에 토큰 없이 요청하면 인증 필요 에러로 응답한다")
    void protectedEndpointWithoutToken() throws Exception {
        mockMvc.perform(get(PROTECTED_URI))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value(CommonErrorType.UNAUTHORIZED.getErrorCode()));
    }

    @Test
    @DisplayName("보호된 엔드포인트에 만료된 토큰으로 요청하면 만료 에러 코드로 응답한다")
    void protectedEndpointWithExpiredToken() throws Exception {
        // given
        given(authenticationUsecase.authenticate(any()))
                .willThrow(new CustomException(BusinessAuthErrorType.EXPIRED_TOKEN));

        // when & then
        mockMvc.perform(get(PROTECTED_URI).header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value(BusinessAuthErrorType.EXPIRED_TOKEN.getErrorCode()));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "/api/v1/auth/sign-in",
            "/api/v1/auth/sign-up",
            "/api/v1/auth/email-verification/issue",
            "/api/v1/auth/email-verification/verify"
    })
    @DisplayName("공개 엔드포인트는 만료된 토큰이 있어도 토큰을 검증하지 않고 요청이 처리된다")
    void publicEndpointWithExpiredToken(String uri) throws Exception {
        mockMvc.perform(post(uri).header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN))
                .andExpect(status().isOk());

        then(authenticationUsecase).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("공개 엔드포인트 목록에 없는 auth 하위 경로는 인증이 필요하다")
    void notListedAuthEndpointRequiresAuthentication() throws Exception {
        mockMvc.perform(post("/api/v1/auth/logout"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value(CommonErrorType.UNAUTHORIZED.getErrorCode()));
    }

    @Configuration
    @EnableWebMvc
    @Import({SecurityConfig.class, SecurityHandlerConfig.class, SecurityErrorResponseWriter.class, TestController.class})
    static class TestConfig {
        @Bean
        AuthenticationUsecase authenticationUsecase() {
            return Mockito.mock(AuthenticationUsecase.class);
        }

        @Bean
        JsonMapper jsonMapper() {
            return JsonMapper.builder().build();
        }
    }

    @RestController
    static class TestController {
        @GetMapping(PROTECTED_URI)
        String me(@AuthenticationPrincipal CustomUserDetails principal) {
            return String.valueOf(principal.getMemberId().getValue());
        }

        @PostMapping({
                "/api/v1/auth/sign-in",
                "/api/v1/auth/sign-up",
                "/api/v1/auth/email-verification/issue",
                "/api/v1/auth/email-verification/verify",
                "/api/v1/auth/logout"
        })
        String ok() {
            return "ok";
        }
    }
}
