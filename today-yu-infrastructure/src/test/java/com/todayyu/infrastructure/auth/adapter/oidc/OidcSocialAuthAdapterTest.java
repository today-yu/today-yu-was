package com.todayyu.infrastructure.auth.adapter.oidc;

import com.todayyu.application.auth.error.BusinessAuthErrorType;
import com.todayyu.common.exception.CustomException;
import com.todayyu.domain.member.vo.AuthProvider;
import com.todayyu.domain.member.vo.AuthProviderUserId;
import com.todayyu.domain.member.vo.SocialIdentity;
import com.todayyu.infrastructure.oidc.client.OidcTokenClient;
import com.todayyu.infrastructure.oidc.dto.OidcTokenResponse;
import com.todayyu.infrastructure.oidc.dto.Subject;
import com.todayyu.infrastructure.oidc.properties.OidcProperties;
import com.todayyu.infrastructure.oidc.verifier.OidcIdTokenVerifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class OidcSocialAuthAdapterTest {
    private static final AuthProvider PROVIDER = AuthProvider.KAKAO;
    private static final String AUTHORIZATION_CODE = "authorization-code";
    private static final String ID_TOKEN = "id-token";
    private static final String SUBJECT = "1234567890";
    private static final OidcProperties.Provider PROPERTIES = new OidcProperties.Provider(
            "test-client-id",
            "test-client-secret",
            "http://localhost/redirect",
            "https://kauth.kakao.com/oauth/token",
            "https://kauth.kakao.com",
            "https://kauth.kakao.com/.well-known/jwks.json"
    );

    @Mock
    private OidcTokenClient oidcTokenClient;

    @Mock
    private OidcIdTokenVerifier oidcIdTokenVerifier;

    private OidcSocialAuthAdapter oidcSocialAuthAdapter;

    @BeforeEach
    void setUp() {
        OidcProperties oidcProperties =
                new OidcProperties(Duration.ofSeconds(3), Duration.ofSeconds(3), Map.of(PROVIDER, PROPERTIES));
        oidcSocialAuthAdapter = new OidcSocialAuthAdapter(oidcProperties, oidcTokenClient, oidcIdTokenVerifier);
    }

    @Test
    @DisplayName("인가 코드를 교환한 ID Token을 검증하여, subject를 인증 제공자 내 사용자 ID로 하는 SocialIdentity를 반환한다")
    void authenticate() {
        // given
        given(oidcTokenClient.exchange(PROPERTIES, AUTHORIZATION_CODE)).willReturn(new OidcTokenResponse(ID_TOKEN));
        given(oidcIdTokenVerifier.verify(PROVIDER, PROPERTIES, ID_TOKEN)).willReturn(new Subject(SUBJECT));

        // when
        SocialIdentity socialIdentity = oidcSocialAuthAdapter.authenticate(PROVIDER, AUTHORIZATION_CODE);

        // then
        assertEquals(new SocialIdentity(PROVIDER, new AuthProviderUserId(SUBJECT)), socialIdentity);
    }

    @Test
    @DisplayName("소셜 인증 제공자가 아니면 인가 코드를 교환하지 않고 예외가 발생한다")
    void authenticateWithLocalProvider() {
        // when & then
        assertThrows(IllegalArgumentException.class,
                () -> oidcSocialAuthAdapter.authenticate(AuthProvider.LOCAL, AUTHORIZATION_CODE));
        then(oidcTokenClient).shouldHaveNoInteractions();
        then(oidcIdTokenVerifier).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("인가 코드 교환에 실패하면 ID Token을 검증하지 않고 예외가 전파된다")
    void authenticateWhenExchangeFails() {
        // given
        given(oidcTokenClient.exchange(PROPERTIES, AUTHORIZATION_CODE))
                .willThrow(new CustomException(BusinessAuthErrorType.FAIL_SOCIAL_SIGNIN));

        // when & then
        CustomException exception = assertThrows(CustomException.class,
                () -> oidcSocialAuthAdapter.authenticate(PROVIDER, AUTHORIZATION_CODE));
        assertEquals(BusinessAuthErrorType.FAIL_SOCIAL_SIGNIN, exception.getErrorType());
        then(oidcIdTokenVerifier).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("ID Token 검증에 실패하면 예외가 전파된다")
    void authenticateWhenVerifyFails() {
        // given
        given(oidcTokenClient.exchange(PROPERTIES, AUTHORIZATION_CODE)).willReturn(new OidcTokenResponse(ID_TOKEN));
        given(oidcIdTokenVerifier.verify(any(), any(), any()))
                .willThrow(new CustomException(BusinessAuthErrorType.FAIL_SOCIAL_SIGNIN));

        // when & then
        CustomException exception = assertThrows(CustomException.class,
                () -> oidcSocialAuthAdapter.authenticate(PROVIDER, AUTHORIZATION_CODE));
        assertEquals(BusinessAuthErrorType.FAIL_SOCIAL_SIGNIN, exception.getErrorType());
    }
}
