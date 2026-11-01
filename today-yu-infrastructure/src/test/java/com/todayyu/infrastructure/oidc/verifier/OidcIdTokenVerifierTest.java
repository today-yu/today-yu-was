package com.todayyu.infrastructure.oidc.verifier;

import com.todayyu.application.auth.error.BusinessAuthErrorType;
import com.todayyu.common.exception.CustomException;
import com.todayyu.domain.member.vo.AuthProvider;
import com.todayyu.infrastructure.oidc.dto.Subject;
import com.todayyu.infrastructure.oidc.properties.OidcProperties;
import com.todayyu.infrastructure.oidc.provider.OidcPublicKeyProvider;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.security.KeyPair;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class OidcIdTokenVerifierTest {
    private static final AuthProvider PROVIDER = AuthProvider.KAKAO;
    private static final String CLIENT_ID = "test-client-id";
    private static final String ISSUER = "https://kauth.kakao.com";
    private static final String JWK_SET_URI = "https://kauth.kakao.com/.well-known/jwks.json";
    private static final String KID = "test-kid";
    private static final String SUBJECT = "1234567890";
    private static final KeyPair KEY_PAIR = Jwts.SIG.RS256.keyPair().build();
    private static final KeyPair OTHER_KEY_PAIR = Jwts.SIG.RS256.keyPair().build();
    private static final OidcProperties.Provider PROPERTIES = new OidcProperties.Provider(
            CLIENT_ID,
            "test-client-secret",
            "http://localhost/redirect",
            "https://kauth.kakao.com/oauth/token",
            ISSUER,
            JWK_SET_URI
    );

    @Mock
    private OidcPublicKeyProvider oidcPublicKeyProvider;

    @InjectMocks
    private OidcIdTokenVerifier oidcIdTokenVerifier;

    @Test
    @DisplayName("유효한 ID Token이면 ID Token의 subject를 반환한다")
    void verify() {
        // given
        givenPublicKey();
        String idToken = idToken().compact();

        // when
        Subject subject = oidcIdTokenVerifier.verify(PROVIDER, PROPERTIES, idToken);

        // then
        assertEquals(SUBJECT, subject.value());
    }

    @Test
    @DisplayName("ID Token 헤더의 kid와 인증 제공자의 공개키 목록 조회 URI로 공개키를 조회한다")
    void verifyLocatesPublicKeyByKid() {
        // given
        givenPublicKey();
        String idToken = idToken().compact();

        // when
        oidcIdTokenVerifier.verify(PROVIDER, PROPERTIES, idToken);

        // then
        then(oidcPublicKeyProvider).should().getPublicKey(PROVIDER, JWK_SET_URI, KID);
    }

    @Test
    @DisplayName("만료 시각이 허용 오차 이내로 지난 ID Token은 유효하다")
    void verifyWithinClockSkew() {
        // given
        givenPublicKey();
        String idToken = idToken()
                .expiration(Date.from(Instant.now().minus(Duration.ofSeconds(30))))
                .compact();

        // when
        Subject subject = oidcIdTokenVerifier.verify(PROVIDER, PROPERTIES, idToken);

        // then
        assertEquals(SUBJECT, subject.value());
    }

    @Test
    @DisplayName("만료 시각이 허용 오차를 넘어 지난 ID Token이면 예외가 발생한다")
    void verifyExpiredIdToken() {
        // given
        givenPublicKey();
        String idToken = idToken()
                .expiration(Date.from(Instant.now().minus(Duration.ofMinutes(2))))
                .compact();

        // when & then
        assertFailSocialSignin(idToken);
    }

    @Test
    @DisplayName("ID Token의 발급자(iss)가 인증 제공자와 다르면 예외가 발생한다")
    void verifyWithInvalidIssuer() {
        // given
        givenPublicKey();
        String idToken = idToken()
                .issuer("https://other-issuer.com")
                .compact();

        // when & then
        assertFailSocialSignin(idToken);
    }

    @Test
    @DisplayName("ID Token의 대상자(aud)가 클라이언트 ID와 다르면 예외가 발생한다")
    void verifyWithInvalidAudience() {
        // given
        givenPublicKey();
        String idToken = idToken("other-client-id").compact();

        // when & then
        assertFailSocialSignin(idToken);
    }

    @Test
    @DisplayName("ID Token이 인증 제공자의 공개키와 다른 키로 서명되었으면 예외가 발생한다")
    void verifyWithInvalidSignature() {
        // given
        givenPublicKey();
        String idToken = idToken()
                .signWith(OTHER_KEY_PAIR.getPrivate())
                .compact();

        // when & then
        assertFailSocialSignin(idToken);
    }

    @Test
    @DisplayName("서명되지 않은 ID Token이면 공개키를 조회하지 않고 예외가 발생한다")
    void verifyUnsignedIdToken() {
        // given
        String idToken = Jwts.builder()
                .issuer(ISSUER)
                .audience().add(CLIENT_ID).and()
                .subject(SUBJECT)
                .expiration(Date.from(Instant.now().plus(Duration.ofHours(1))))
                .compact();

        // when & then
        assertFailSocialSignin(idToken);
        then(oidcPublicKeyProvider).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("공개키 조회에 실패하면 예외가 발생한다")
    void verifyWhenPublicKeyNotFound() {
        // given
        given(oidcPublicKeyProvider.getPublicKey(any(), any(), any()))
                .willThrow(new JwtException("kid에 해당하는 공개키가 존재하지 않습니다."));
        String idToken = idToken().compact();

        // when & then
        assertFailSocialSignin(idToken);
    }

    @Test
    @DisplayName("ID Token에 subject가 없으면 예외가 발생한다")
    void verifyWithoutSubject() {
        // given
        givenPublicKey();
        String idToken = idToken()
                .subject(null)
                .compact();

        // when & then
        assertFailSocialSignin(idToken);
    }

    @Test
    @DisplayName("ID Token 형식이 올바르지 않으면 예외가 발생한다")
    void verifyMalformedIdToken() {
        // when & then
        assertFailSocialSignin("malformed-id-token");
        assertFailSocialSignin(null);
    }

    private void givenPublicKey() {
        given(oidcPublicKeyProvider.getPublicKey(PROVIDER, JWK_SET_URI, KID)).willReturn(KEY_PAIR.getPublic());
    }

    private void assertFailSocialSignin(String idToken) {
        CustomException exception = assertThrows(CustomException.class,
                () -> oidcIdTokenVerifier.verify(PROVIDER, PROPERTIES, idToken));
        assertEquals(BusinessAuthErrorType.FAIL_SOCIAL_SIGNIN, exception.getErrorType());
    }

    private static JwtBuilder idToken() {
        return idToken(CLIENT_ID);
    }

    private static JwtBuilder idToken(String audience) {
        Instant now = Instant.now();

        return Jwts.builder()
                .header().keyId(KID).and()
                .issuer(ISSUER)
                .audience().add(audience).and()
                .subject(SUBJECT)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(Duration.ofHours(1))))
                .signWith(KEY_PAIR.getPrivate());
    }
}
