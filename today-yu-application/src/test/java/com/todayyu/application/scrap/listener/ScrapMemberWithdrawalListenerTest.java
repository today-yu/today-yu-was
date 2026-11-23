package com.todayyu.application.scrap.listener;

import com.todayyu.application.member.event.MemberWithdrawalEvent;
import com.todayyu.application.scrap.port.save.SaveScrapPort;
import com.todayyu.domain.member.vo.MemberId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;

@ExtendWith(MockitoExtension.class)
class ScrapMemberWithdrawalListenerTest {
    private static final MemberId MEMBER_ID = new MemberId(1L);

    @Mock
    private SaveScrapPort saveScrapPort;

    @InjectMocks
    private ScrapMemberWithdrawalListener listener;

    @Test
    @DisplayName("탈퇴한 회원의 스크랩을 모두 삭제한다")
    void deleteScraps() {
        // when
        listener.deleteScraps(new MemberWithdrawalEvent(MEMBER_ID, List.of()));

        // then
        then(saveScrapPort).should().deleteAllByMemberId(MEMBER_ID);
        then(saveScrapPort).shouldHaveNoMoreInteractions();
    }

    @Test
    @DisplayName("스크랩 삭제 중 예외가 발생하면 회원 탈퇴가 롤백되도록 예외를 그대로 전파한다")
    void deleteScrapsWithException() {
        // given
        willThrow(new IllegalStateException()).given(saveScrapPort).deleteAllByMemberId(MEMBER_ID);

        // when & then
        assertThrows(
                IllegalStateException.class,
                () -> listener.deleteScraps(new MemberWithdrawalEvent(MEMBER_ID, List.of()))
        );
    }
}
