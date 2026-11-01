package com.todayyu.infrastructure.oidc.provider;

import com.todayyu.domain.member.vo.AuthProvider;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Jwks;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.net.URI;
import java.security.Key;
import java.security.KeyPair;
import java.security.interfaces.RSAPublicKey;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class OidcPublicKeyProviderTest {
    private static final AuthProvider PROVIDER = AuthProvider.KAKAO;
    private static final String JWK_SET_URI = "https://kauth.kakao.com/.well-known/jwks.json";
    private static final String KID = "test-kid";
    private static final String UNKNOWN_KID = "unknown-kid";
    private static final KeyPair KEY_PAIR = Jwts.SIG.RS256.keyPair().build();

    private MockRestServiceServer server;
    private OidcPublicKeyProvider oidcPublicKeyProvider;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        oidcPublicKeyProvider = new OidcPublicKeyProvider(builder.build());
    }

    @Test
    @DisplayName("공개키 목록이 없으면 공개키 목록 조회 API를 호출하여 kid에 해당하는 공개키를 반환한다")
    void getPublicKeyFetchesPublicKeys() {
        // given
        expectJwkSetRequest();

        // when
        Key key = oidcPublicKeyProvider.getPublicKey(PROVIDER, JWK_SET_URI, KID);

        // then
        assertEquals(KEY_PAIR.getPublic(), key);
        server.verify();
    }

    @Test
    @DisplayName("이미 조회한 공개키 목록에 kid가 존재하면 공개키 목록 조회 API를 다시 호출하지 않는다")
    void getPublicKeyUsesCachedPublicKeys() {
        // given
        expectJwkSetRequest();
        oidcPublicKeyProvider.getPublicKey(PROVIDER, JWK_SET_URI, KID);

        // when
        Key key = oidcPublicKeyProvider.getPublicKey(PROVIDER, JWK_SET_URI, KID);

        // then
        assertEquals(KEY_PAIR.getPublic(), key);
        server.verify();
    }

    @Test
    @DisplayName("공개키 목록 갱신 최소 기간 이내에는 kid가 존재하지 않아도 공개키 목록을 다시 조회하지 않고 예외가 발생한다")
    void getPublicKeyWithUnknownKidWithinMinFetchInterval() {
        // given
        expectJwkSetRequest();
        oidcPublicKeyProvider.getPublicKey(PROVIDER, JWK_SET_URI, KID);

        // when & then
        assertThrows(JwtException.class,
                () -> oidcPublicKeyProvider.getPublicKey(PROVIDER, JWK_SET_URI, UNKNOWN_KID));
        server.verify();
    }

    @Test
    @DisplayName("ID Token 헤더에 kid가 없으면 공개키 목록을 조회하지 않고 예외가 발생한다")
    void getPublicKeyWithBlankKid() {
        // when & then
        assertThrows(JwtException.class,
                () -> oidcPublicKeyProvider.getPublicKey(PROVIDER, JWK_SET_URI, null));
        assertThrows(JwtException.class,
                () -> oidcPublicKeyProvider.getPublicKey(PROVIDER, JWK_SET_URI, " "));
        server.verify();
    }

    @Test
    @DisplayName("공개키 목록 조회 API 호출에 실패하면 예외가 발생한다")
    void getPublicKeyWhenFetchFails() {
        // given
        server.expect(once(), request -> assertEquals(URI.create(JWK_SET_URI), request.getURI()))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withServerError());

        // when & then
        assertThrows(JwtException.class,
                () -> oidcPublicKeyProvider.getPublicKey(PROVIDER, JWK_SET_URI, KID));
        server.verify();
    }

    @Test
    @DisplayName("공개키 목록 응답 형식이 올바르지 않으면 예외가 발생한다")
    void getPublicKeyWithMalformedJwkSet() {
        // given
        server.expect(once(), request -> assertEquals(URI.create(JWK_SET_URI), request.getURI()))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("not-json", MediaType.APPLICATION_JSON));

        // when & then
        assertThrows(JwtException.class,
                () -> oidcPublicKeyProvider.getPublicKey(PROVIDER, JWK_SET_URI, KID));
        server.verify();
    }

    private void expectJwkSetRequest() {
        server.expect(once(), request -> assertEquals(URI.create(JWK_SET_URI), request.getURI()))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(jwkSet(), MediaType.APPLICATION_JSON));
    }

    private static String jwkSet() {
        String jwk = Jwks.json(Jwks.builder()
                .key((RSAPublicKey) KEY_PAIR.getPublic())
                .id(KID)
                .build());

        return "{\"keys\":[" + jwk + "]}";
    }
}
