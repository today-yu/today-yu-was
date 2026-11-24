package com.todayyu.infrastructure.auth.adapter.social;

import com.todayyu.application.auth.error.BusinessAuthErrorType;
import com.todayyu.common.exception.CustomException;
import com.todayyu.domain.member.vo.AuthProvider;
import com.todayyu.domain.member.vo.AuthProviderUserId;
import com.todayyu.infrastructure.kakao.client.KakaoUnlinkClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KakaoSocialUnlinkStrategy implements SocialUnlinkStrategy {
    private final KakaoUnlinkClient unlinkClient;
    
    /**
     * 연결 해제 전략이 담당하는 인증 제공자를 반환하는 메서드
     *
     * @return 카카오 인증 제공자 {@link AuthProvider#KAKAO}
     */
    @Override
    public AuthProvider getAuthProvider() {
        return AuthProvider.KAKAO;
    }

    /**
     * 카카오에서 서비스와 사용자의 연결을 해제하는 메서드
     *
     * <p> 인증 제공자 내 사용자 ID({@link AuthProviderUserId})를 카카오 회원번호로 사용하여,
     *     {@link KakaoUnlinkClient}에 연결 해제를 요청한다. </p>
     *
     * @param authProviderUserId 연결을 해제할 사용자의 카카오 회원번호
     * @throws CustomException {@link BusinessAuthErrorType#FAIL_SOCIAL_UNLINK} - 카카오 연결 해제에 실패한 경우
     */
    @Override
    public void unlink(AuthProviderUserId authProviderUserId) {
        unlinkClient.unlink(authProviderUserId.getValue());
    }
}
