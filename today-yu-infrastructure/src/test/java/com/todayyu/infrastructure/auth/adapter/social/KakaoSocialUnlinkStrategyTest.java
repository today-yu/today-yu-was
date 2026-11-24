package com.todayyu.infrastructure.auth.adapter.social;

import com.todayyu.application.auth.error.BusinessAuthErrorType;
import com.todayyu.common.exception.CustomException;
import com.todayyu.domain.member.vo.AuthProvider;
import com.todayyu.domain.member.vo.AuthProviderUserId;
import com.todayyu.infrastructure.kakao.client.KakaoUnlinkClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;

@ExtendWith(MockitoExtension.class)
class KakaoSocialUnlinkStrategyTest {
    private static final String KAKAO_USER_ID = "1234567890";
    private static final AuthProviderUserId AUTH_PROVIDER_USER_ID = new AuthProviderUserId(KAKAO_USER_ID);

    @Mock
    private KakaoUnlinkClient kakaoUnlinkClient;

    @InjectMocks
    private KakaoSocialUnlinkStrategy kakaoSocialUnlinkStrategy;

    @Test
    @DisplayName("카카오 인증 제공자를 담당한다")
    void getAuthProvider() {
        // when & then
        assertEquals(AuthProvider.KAKAO, kakaoSocialUnlinkStrategy.getAuthProvider());
    }

    @Test
    @DisplayName("인증 제공자 내 사용자 ID를 카카오 회원번호로 하여 카카오 연결 해제를 요청한다")
    void unlink() {
        // when
        kakaoSocialUnlinkStrategy.unlink(AUTH_PROVIDER_USER_ID);

        // then
        then(kakaoUnlinkClient).should().unlink(KAKAO_USER_ID);
        then(kakaoUnlinkClient).shouldHaveNoMoreInteractions();
    }

    @Test
    @DisplayName("카카오 연결 해제에 실패하면 예외를 그대로 전파한다")
    void unlinkWithClientException() {
        // given
        CustomException exception = new CustomException(BusinessAuthErrorType.FAIL_SOCIAL_UNLINK);
        willThrow(exception).given(kakaoUnlinkClient).unlink(KAKAO_USER_ID);

        // when & then
        CustomException thrown = assertThrows(
                CustomException.class,
                () -> kakaoSocialUnlinkStrategy.unlink(AUTH_PROVIDER_USER_ID)
        );
        assertSame(exception, thrown);
    }
}
