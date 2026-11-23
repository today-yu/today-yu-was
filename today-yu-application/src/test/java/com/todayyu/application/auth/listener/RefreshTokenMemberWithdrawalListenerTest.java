package com.todayyu.application.auth.listener;

import com.todayyu.application.auth.port.save.SaveRefreshTokenPort;
import com.todayyu.application.member.event.MemberWithdrawalEvent;
import com.todayyu.domain.member.vo.MemberId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;

@ExtendWith(MockitoExtension.class)
class RefreshTokenMemberWithdrawalListenerTest {
    private static final MemberId MEMBER_ID = new MemberId(1L);

    @Mock
    private SaveRefreshTokenPort saveRefreshTokenPort;

    @InjectMocks
    private RefreshTokenMemberWithdrawalListener listener;

    @Test
    @DisplayName("탈퇴한 회원의 Refresh Token을 모두 삭제한다")
    void deleteRefreshTokens() {
        // when
        listener.deleteRefreshTokens(new MemberWithdrawalEvent(MEMBER_ID, List.of()));

        // then
        then(saveRefreshTokenPort).should().deleteAllByMemberId(MEMBER_ID);
        then(saveRefreshTokenPort).shouldHaveNoMoreInteractions();
    }

    @Test
    @DisplayName("Refresh Token 삭제 중 예외가 발생해도 회원 탈퇴에 영향을 주지 않도록 예외를 전파하지 않는다")
    void deleteRefreshTokensWithException() {
        // given
        willThrow(new IllegalStateException()).given(saveRefreshTokenPort).deleteAllByMemberId(MEMBER_ID);

        // when & then
        assertDoesNotThrow(() -> listener.deleteRefreshTokens(new MemberWithdrawalEvent(MEMBER_ID, List.of())));
    }
}
