package com.todayyu.application.auth.listener;

import com.todayyu.application.auth.port.SocialUnlinkPort;
import com.todayyu.application.member.event.MemberWithdrawalEvent;
import com.todayyu.domain.member.vo.AuthProvider;
import com.todayyu.domain.member.vo.AuthProviderUserId;
import com.todayyu.domain.member.vo.MemberId;
import com.todayyu.domain.member.vo.SocialIdentity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;

@ExtendWith(MockitoExtension.class)
class SocialUnlinkMemberWithdrawalListenerTest {
    private static final MemberId MEMBER_ID = new MemberId(1L);
    private static final SocialIdentity FIRST_IDENTITY =
            new SocialIdentity(AuthProvider.KAKAO, new AuthProviderUserId("1111"));
    private static final SocialIdentity SECOND_IDENTITY =
            new SocialIdentity(AuthProvider.KAKAO, new AuthProviderUserId("2222"));

    @Mock
    private SocialUnlinkPort socialUnlinkPort;

    @InjectMocks
    private SocialUnlinkMemberWithdrawalListener listener;

    @Test
    @DisplayName("탈퇴한 회원의 소셜 인증 정보마다 연결 해제를 요청한다")
    void unlinkSocialIdentities() {
        // given
        MemberWithdrawalEvent event = new MemberWithdrawalEvent(MEMBER_ID, List.of(FIRST_IDENTITY, SECOND_IDENTITY));

        // when
        listener.unlinkSocialIdentities(event);

        // then
        then(socialUnlinkPort).should().unlink(FIRST_IDENTITY);
        then(socialUnlinkPort).should().unlink(SECOND_IDENTITY);
        then(socialUnlinkPort).shouldHaveNoMoreInteractions();
    }

    @Test
    @DisplayName("소셜 인증 정보가 없는 회원은 연결 해제를 요청하지 않는다")
    void unlinkSocialIdentitiesWithoutSocialIdentity() {
        // when
        listener.unlinkSocialIdentities(new MemberWithdrawalEvent(MEMBER_ID, List.of()));

        // then
        then(socialUnlinkPort).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("연결 해제 중 예외가 발생해도 회원 탈퇴에 영향을 주지 않도록 예외를 전파하지 않는다")
    void unlinkSocialIdentitiesWithException() {
        // given
        willThrow(new IllegalStateException()).given(socialUnlinkPort).unlink(any());

        // when & then
        assertDoesNotThrow(() -> listener.unlinkSocialIdentities(
                new MemberWithdrawalEvent(MEMBER_ID, List.of(FIRST_IDENTITY))
        ));
    }

    @Test
    @DisplayName("한 소셜 인증 정보의 연결 해제에 실패해도 나머지 소셜 인증 정보의 연결 해제를 요청한다")
    void unlinkSocialIdentitiesContinuesAfterFailure() {
        // given
        willThrow(new IllegalStateException()).given(socialUnlinkPort).unlink(FIRST_IDENTITY);
        MemberWithdrawalEvent event = new MemberWithdrawalEvent(MEMBER_ID, List.of(FIRST_IDENTITY, SECOND_IDENTITY));

        // when
        listener.unlinkSocialIdentities(event);

        // then
        then(socialUnlinkPort).should().unlink(FIRST_IDENTITY);
        then(socialUnlinkPort).should().unlink(SECOND_IDENTITY);
    }
}
