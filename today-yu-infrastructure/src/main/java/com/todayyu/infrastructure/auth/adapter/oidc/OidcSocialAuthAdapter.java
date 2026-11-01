package com.todayyu.infrastructure.auth.adapter.oidc;

import com.todayyu.application.auth.port.SocialAuthPort;
import com.todayyu.domain.member.vo.AuthProvider;
import com.todayyu.domain.member.vo.AuthProviderUserId;
import com.todayyu.domain.member.vo.SocialIdentity;
import com.todayyu.infrastructure.common.annotation.Adapter;
import com.todayyu.infrastructure.oidc.client.OidcTokenClient;
import com.todayyu.infrastructure.oidc.dto.OidcTokenResponse;
import com.todayyu.infrastructure.oidc.dto.Subject;
import com.todayyu.infrastructure.oidc.properties.OidcProperties;
import com.todayyu.infrastructure.oidc.verifier.OidcIdTokenVerifier;
import lombok.RequiredArgsConstructor;

@Adapter
@RequiredArgsConstructor
public class OidcSocialAuthAdapter implements SocialAuthPort {
    private final OidcProperties oidcProperties;
    private final OidcTokenClient oidcTokenClient;
    private final OidcIdTokenVerifier oidcIdTokenVerifier;
    
    /**
     * 인증 제공자({@link AuthProvider})의 인가 코드로 사용자를 인증하는 메서드
     *
     * <p> 인증 제공자의 OIDC 설정({@link OidcProperties.Provider})으로 인가 코드를 토큰으로 교환하여 ID Token을 발급받는다. </p>
     *
     * <p> 발급받은 ID Token의 서명과 클레임(iss, aud, exp)을 검증한 뒤,
     *     sub 클레임을 인증 제공자 내 사용자 ID({@link AuthProviderUserId})로 하는 {@link SocialIdentity}를 반환한다. </p>
     *
     * @param provider 인증 제공자
     * @param authorizationCode 인증 제공자로부터 발급받은 인가 코드
     * @return 인증 제공자 내 사용자 식별 정보 {@link SocialIdentity} 객체
     * @throws IllegalArgumentException 소셜 인증 제공자가 아닌 경우
     */
    @Override
    public SocialIdentity authenticate(AuthProvider provider, String authorizationCode) {
        OidcProperties.Provider properties = oidcProperties.getProvider(provider);
        OidcTokenResponse response = oidcTokenClient.exchange(properties, authorizationCode);
        Subject subject = oidcIdTokenVerifier.verify(provider, properties, response.idToken());
        
        return new SocialIdentity(provider, new AuthProviderUserId(subject.value()));
    }
}
