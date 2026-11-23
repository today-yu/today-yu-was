package com.todayyu.application.info.listener;

import com.todayyu.application.info.port.save.SaveHelpfulMarkPort;
import com.todayyu.application.member.event.MemberWithdrawalEvent;
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
class HelpfulMarkMemberWithdrawalListenerTest {
    private static final MemberId MEMBER_ID = new MemberId(1L);

    @Mock
    private SaveHelpfulMarkPort saveHelpfulMarkPort;

    @InjectMocks
    private HelpfulMarkMemberWithdrawalListener listener;

    @Test
    @DisplayName("탈퇴한 회원이 남긴 '도움이 되는 정보에요'를 모두 삭제한다")
    void deleteHelpfulMarks() {
        // when
        listener.deleteHelpfulMarks(new MemberWithdrawalEvent(MEMBER_ID, List.of()));

        // then
        then(saveHelpfulMarkPort).should().deleteAllByMemberId(MEMBER_ID);
        then(saveHelpfulMarkPort).shouldHaveNoMoreInteractions();
    }

    @Test
    @DisplayName("'도움이 되는 정보에요' 삭제 중 예외가 발생하면 회원 탈퇴가 롤백되도록 예외를 그대로 전파한다")
    void deleteHelpfulMarksWithException() {
        // given
        willThrow(new IllegalStateException()).given(saveHelpfulMarkPort).deleteAllByMemberId(MEMBER_ID);

        // when & then
        assertThrows(
                IllegalStateException.class,
                () -> listener.deleteHelpfulMarks(new MemberWithdrawalEvent(MEMBER_ID, List.of()))
        );
    }
}
