package com.todayyu.application.auth.listener;

import com.todayyu.application.auth.port.save.SaveRefreshTokenPort;
import com.todayyu.application.member.event.MemberWithdrawalEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class RefreshTokenMemberWithdrawalListener {
    private final SaveRefreshTokenPort saveRefreshTokenPort;

    /**
     * 탈퇴한 회원의 Refresh Token을 모두 삭제하는 탈퇴 회원 이벤트 리스너 메서드
     *
     * <p> 세션과 관계없이 해당 회원의 Refresh Token을 모두 삭제한다. </p>
     *
     * <p> 회원 탈퇴 트랜잭션이 커밋된 이후에 실행되며, 처리 중 예외가 발생해도 회원 탈퇴는 유지된다. <p>
     *
     * <p> {@link RuntimeException}이 발생하면 전파하지 않고 로그로 남긴다. </p>
     *
     * @param event 회원 탈퇴 이벤트
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void deleteRefreshTokens(MemberWithdrawalEvent event) {
        try {
            saveRefreshTokenPort.deleteAllByMemberId(event.memberId());
        } catch (RuntimeException e) {
            log.warn(
                    "[Error/Redis] 탈퇴 회원의 Refresh Token 삭제에 실패하였습니다. (eventId: {} / memberId: {} / message: {})",
                    event.eventId().getValue(),
                    event.memberId().getValue(),
                    e.getMessage()
            );
        }
    }
}
