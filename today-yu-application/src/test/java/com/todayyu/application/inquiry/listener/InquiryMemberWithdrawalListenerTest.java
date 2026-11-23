package com.todayyu.application.inquiry.listener;

import com.todayyu.application.inquiry.port.save.SaveInquiryPort;
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
class InquiryMemberWithdrawalListenerTest {
    private static final MemberId MEMBER_ID = new MemberId(1L);

    @Mock
    private SaveInquiryPort saveInquiryPort;

    @InjectMocks
    private InquiryMemberWithdrawalListener listener;

    @Test
    @DisplayName("탈퇴한 회원이 작성한 문의를 상태와 관계없이 모두 삭제한다")
    void deleteInquiries() {
        // when
        listener.deleteInquiries(new MemberWithdrawalEvent(MEMBER_ID, List.of()));

        // then
        then(saveInquiryPort).should().deleteAllByMemberId(MEMBER_ID);
        then(saveInquiryPort).shouldHaveNoMoreInteractions();
    }

    @Test
    @DisplayName("문의 삭제 중 예외가 발생하면 회원 탈퇴가 롤백되도록 예외를 그대로 전파한다")
    void deleteInquiriesWithException() {
        // given
        willThrow(new IllegalStateException()).given(saveInquiryPort).deleteAllByMemberId(MEMBER_ID);

        // when & then
        assertThrows(
                IllegalStateException.class,
                () -> listener.deleteInquiries(new MemberWithdrawalEvent(MEMBER_ID, List.of()))
        );
    }
}
