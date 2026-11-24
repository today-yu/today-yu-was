package com.todayyu.infrastructure.auth.adapter.social;

import com.todayyu.domain.member.vo.AuthProvider;
import com.todayyu.domain.member.vo.AuthProviderUserId;
import com.todayyu.domain.member.vo.SocialIdentity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;

@ExtendWith(MockitoExtension.class)
class SocialUnlinkAdapterTest {
    private static final AuthProviderUserId AUTH_PROVIDER_USER_ID = new AuthProviderUserId("1234567890");
    private static final SocialIdentity KAKAO_IDENTITY =
            new SocialIdentity(AuthProvider.KAKAO, AUTH_PROVIDER_USER_ID);

    @Mock
    private SocialUnlinkStrategy kakaoUnlinkStrategy;

    @Test
    @DisplayName("인증 제공자에 해당하는 연결 해제 전략에 인증 제공자 내 사용자 ID로 연결 해제를 위임한다")
    void unlink() {
        // given
        given(kakaoUnlinkStrategy.getAuthProvider()).willReturn(AuthProvider.KAKAO);
        SocialUnlinkAdapter socialUnlinkAdapter = new SocialUnlinkAdapter(List.of(kakaoUnlinkStrategy));

        // when
        socialUnlinkAdapter.unlink(KAKAO_IDENTITY);

        // then
        then(kakaoUnlinkStrategy).should().unlink(AUTH_PROVIDER_USER_ID);
    }

    @Test
    @DisplayName("인증 제공자에 해당하는 연결 해제 전략이 없으면 예외가 발생한다")
    void unlinkWithoutStrategy() {
        // given
        SocialUnlinkAdapter socialUnlinkAdapter = new SocialUnlinkAdapter(List.of());

        // when & then
        assertThrows(IllegalStateException.class, () -> socialUnlinkAdapter.unlink(KAKAO_IDENTITY));
    }

    @Test
    @DisplayName("연결 해제 전략에서 발생한 예외는 그대로 전파된다")
    void unlinkWithStrategyException() {
        // given
        given(kakaoUnlinkStrategy.getAuthProvider()).willReturn(AuthProvider.KAKAO);
        IllegalStateException exception = new IllegalStateException();
        willThrow(exception).given(kakaoUnlinkStrategy).unlink(AUTH_PROVIDER_USER_ID);
        SocialUnlinkAdapter socialUnlinkAdapter = new SocialUnlinkAdapter(List.of(kakaoUnlinkStrategy));

        // when & then
        assertThrows(IllegalStateException.class, () -> socialUnlinkAdapter.unlink(KAKAO_IDENTITY));
    }
}
