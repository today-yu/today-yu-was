package com.todayyu.infrastructure.oidc.verifier;

import com.todayyu.application.auth.error.BusinessAuthErrorType;
import com.todayyu.common.exception.CustomException;
import com.todayyu.domain.member.vo.AuthProvider;
import com.todayyu.infrastructure.oidc.dto.Subject;
import com.todayyu.infrastructure.oidc.properties.OidcProperties;
import com.todayyu.infrastructure.oidc.provider.OidcPublicKeyProvider;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwsHeader;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.LocatorAdapter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.security.Key;

@Component
@RequiredArgsConstructor
public class OidcIdTokenVerifier {
    private final OidcPublicKeyProvider oidcPublicKeyProvider;
    
    /* 인증 제공자 서버와 시간 차이를 감안하여 허용하기 위한 오차 */
    private static final long CLOCK_SKEW_SECONDS = 60L;
    
    /**
     * ID Token이 유효한지 확인하여 인증 제공자 서버에서 회원의 고유 식별 번호(subject)를 반환하는 메서드
     *
     * <p> {@code idToken}을 파싱하여 토큰의 subject로 기록된 회원 고유 식별 번호를 반환한다. </p>
     *
     * @param provider   인증 제공자
     * @param properties 인증 관련 프로퍼티
     * @param idToken    ID Token
     * @return 인증 제공자 서버내 사용자 고유 번호
     * @throws CustomException {@link BusinessAuthErrorType#FAIL_SOCIAL_SIGNIN}
     *         - ID Token 검증에 실패하거나 Subject가 비어있는 경우
     */
    public Subject verify(AuthProvider provider, OidcProperties.Provider properties, String idToken) {
        Claims claims = parseClaims(provider, properties, idToken);
        String subject = claims.getSubject();
        
        if (subject == null || subject.isBlank()) {
            throw new CustomException(BusinessAuthErrorType.FAIL_SOCIAL_SIGNIN, "ID Token의 Subject가 비어있습니다.");
        }
        
        return new Subject(subject);
    }
    
    /**
     * ID Token 파싱 메서드
     *
     * <p> 토큰 파싱을 위한 공개키는 {@link OidcPublicKeyProvider#getPublicKey(AuthProvider, String, String)}를 호출하여
     *      해당 인증 제공자가 가지는 공개키 목록에서 ID Token 헤더의 kid(Key Id)와 일치하는 공개키를 사용한다. </p>
     *
     * @param provider 인증 제공자
     * @param properties 인증 제공자의 OIDC 인증 관련 프로퍼티
     * @param idToken ID Token
     * @return ID Token의 클레임
     */
    private Claims parseClaims(AuthProvider provider, OidcProperties.Provider properties, String idToken) {
        try {
            return Jwts.parser()
                    .keyLocator(new LocatorAdapter<Key>() {
                        @Override
                        protected Key locate(JwsHeader header) {
                            return oidcPublicKeyProvider.getPublicKey(provider, properties.jwkSetUri(), header.getKeyId());
                        }
                    })
                    .requireIssuer(properties.issuer())
                    .requireAudience(properties.clientId())
                    .clockSkewSeconds(CLOCK_SKEW_SECONDS)
                    .build()
                    .parseSignedClaims(idToken)
                    .getPayload();
        } catch (JwtException | IllegalArgumentException e) {
            throw new CustomException(BusinessAuthErrorType.FAIL_SOCIAL_SIGNIN, "ID Token 검증에 실패하였습니다.", e);
        }
    }
}
