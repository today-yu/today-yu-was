package com.todayyu.apis.common.security.filter;

import com.todayyu.apis.common.security.handler.SecurityErrorResponseWriter;
import com.todayyu.apis.common.security.principal.CustomUserDetails;
import com.todayyu.application.auth.dto.result.AuthenticatedMemberResult;
import com.todayyu.application.auth.error.BusinessAuthErrorType;
import com.todayyu.application.auth.usecase.AuthenticationUsecase;
import com.todayyu.common.exception.CustomException;
import com.todayyu.domain.auth.vo.AccessToken;
import com.todayyu.domain.member.vo.MemberId;
import com.todayyu.domain.member.vo.Role;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {
    private static final String PROTECTED_URI = "/api/v1/members/me";
    private static final String PUBLIC_URI = "/api/v1/auth/sign-in";
    private static final MemberId MEMBER_ID = new MemberId(1L);

    private final JsonMapper jsonMapper = JsonMapper.builder().build();

    @Mock
    private AuthenticationUsecase authenticationUsecase;

    @Captor
    private ArgumentCaptor<AccessToken> accessTokenCaptor;

    private JwtAuthenticationFilter filter;
    private MockHttpServletResponse response;
    private MockFilterChain filterChain;

    @BeforeEach
    void setUp() {
        filter = new JwtAuthenticationFilter(
                authenticationUsecase,
                new SecurityErrorResponseWriter(jsonMapper),
                PathPatternRequestMatcher.pathPattern(PUBLIC_URI)
        );
        response = new MockHttpServletResponse();
        filterChain = new MockFilterChain();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("유효한 Bearer 토큰이면 회원 아이디와 권한으로 인증 정보를 등록하고 다음 필터를 호출한다")
    void doFilterWithValidToken() throws Exception {
        // given
        MockHttpServletRequest request = request(PROTECTED_URI, "Bearer access-token");
        given(authenticationUsecase.authenticate(any()))
                .willReturn(new AuthenticatedMemberResult(MEMBER_ID, Role.MEMBER));

        // when
        filter.doFilter(request, response, filterChain);

        // then
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails principal = assertInstanceOf(CustomUserDetails.class, authentication.getPrincipal());
        assertEquals(MEMBER_ID, principal.getMemberId());
        assertEquals("ROLE_MEMBER", authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElseThrow());
        assertNotNull(filterChain.getRequest());
    }

    @Test
    @DisplayName("Authorization 헤더에서 Bearer 접두사를 제외한 토큰만 추출해 인증을 요청한다")
    void doFilterExtractsTokenWithoutPrefix() throws Exception {
        // given
        MockHttpServletRequest request = request(PROTECTED_URI, "Bearer access-token");
        given(authenticationUsecase.authenticate(any()))
                .willReturn(new AuthenticatedMemberResult(MEMBER_ID, Role.MEMBER));

        // when
        filter.doFilter(request, response, filterChain);

        // then
        then(authenticationUsecase).should().authenticate(accessTokenCaptor.capture());
        assertEquals("access-token", accessTokenCaptor.getValue().getValue());
    }

    @Test
    @DisplayName("Authorization 헤더가 없으면 인증 정보를 등록하지 않고 다음 필터를 호출한다")
    void doFilterWithoutAuthorization() throws Exception {
        // given
        MockHttpServletRequest request = request(PROTECTED_URI, null);

        // when
        filter.doFilter(request, response, filterChain);

        // then
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        assertNotNull(filterChain.getRequest());
        then(authenticationUsecase).shouldHaveNoInteractions();
    }

    @ParameterizedTest
    @ValueSource(strings = {"Basic abc", "Bearerabc", "access-token"})
    @DisplayName("Bearer 스킴이 아니면 인증 정보를 등록하지 않고 다음 필터를 호출한다")
    void doFilterWithNotBearerScheme(String authorization) throws Exception {
        // given
        MockHttpServletRequest request = request(PROTECTED_URI, authorization);

        // when
        filter.doFilter(request, response, filterChain);

        // then
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        assertNotNull(filterChain.getRequest());
        then(authenticationUsecase).shouldHaveNoInteractions();
    }

    @ParameterizedTest
    @ValueSource(strings = {"Bearer ", "Bearer    "})
    @DisplayName("Bearer 토큰이 비어있으면 유효하지 않은 토큰으로 응답하고 다음 필터를 호출하지 않는다")
    void doFilterWithBlankToken(String authorization) throws Exception {
        // given
        MockHttpServletRequest request = request(PROTECTED_URI, authorization);

        // when
        filter.doFilter(request, response, filterChain);

        // then
        assertErrorResponse(BusinessAuthErrorType.INVALID_TOKEN);
        assertNull(filterChain.getRequest());
        then(authenticationUsecase).shouldHaveNoInteractions();
    }

    @ParameterizedTest
    @EnumSource(value = BusinessAuthErrorType.class, names = {"INVALID_TOKEN", "EXPIRED_TOKEN"})
    @DisplayName("토큰 인증에 실패하면 실패 원인의 에러 코드로 응답하고 다음 필터를 호출하지 않는다")
    void doFilterWithAuthenticationFailure(BusinessAuthErrorType errorType) throws Exception {
        // given
        MockHttpServletRequest request = request(PROTECTED_URI, "Bearer access-token");
        given(authenticationUsecase.authenticate(any()))
                .willThrow(new CustomException(errorType));

        // when
        filter.doFilter(request, response, filterChain);

        // then
        assertErrorResponse(errorType);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        assertNull(filterChain.getRequest());
    }

    @Test
    @DisplayName("공개 엔드포인트는 토큰이 있어도 검증하지 않고 다음 필터를 호출한다")
    void doFilterWithPublicEndpoint() throws Exception {
        // given
        MockHttpServletRequest request = request(PUBLIC_URI, "Bearer expired-token");

        // when
        filter.doFilter(request, response, filterChain);

        // then
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        assertNotNull(filterChain.getRequest());
        then(authenticationUsecase).shouldHaveNoInteractions();
    }

    private MockHttpServletRequest request(String uri, String authorization) {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", uri);
        if (authorization != null) {
            request.addHeader(HttpHeaders.AUTHORIZATION, authorization);
        }
        return request;
    }

    private void assertErrorResponse(BusinessAuthErrorType errorType) throws Exception {
        JsonNode body = jsonMapper.readTree(response.getContentAsString());
        assertEquals(401, response.getStatus());
        assertEquals(errorType.getErrorCode(), body.get("errorCode").asString());
    }
}
