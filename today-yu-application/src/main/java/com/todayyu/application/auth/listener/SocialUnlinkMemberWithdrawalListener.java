package com.todayyu.application.auth.listener;

import com.todayyu.application.auth.port.SocialUnlinkPort;
import com.todayyu.application.member.event.MemberWithdrawalEvent;
import com.todayyu.domain.member.vo.SocialIdentity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class SocialUnlinkMemberWithdrawalListener {
    private final SocialUnlinkPort socialUnlinkPort;
    
    /**
     * 탈퇴한 회원이 연동된 소셜 인증의 연결을 해제하는 탈퇴 회원 이벤트 리스너 메서드
     *
     * <p> 소셜 인증 정보({@link SocialIdentity})는 개별로 연결 해제 처리를 진행한다. </p>
     *
     * <p> 소셜 인증의 연결 해제 중 {@link RuntimeException}이 발생하면 전파하지 않고 로그로 남긴다. </p>
     *
     * @param event 회원 탈퇴 이벤트
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void unlinkSocialIdentities(MemberWithdrawalEvent event) {
        for (SocialIdentity socialIdentity : event.socialIdentities()) {
            try {
                socialUnlinkPort.unlink(socialIdentity);
            } catch (RuntimeException e) {
                log.warn(
                    "[Error/Auth] 탈퇴 회원의 소셜 인증 연결 해제에 실패하였습니다. (eventId: {} / memberId: {} / provider: {} / message: {})",
                    event.eventId().getValue(),
                    event.memberId().getValue(),
                    socialIdentity.getAuthProvider().name(),
                    e.getMessage()
                );
            }
        }
    }
}
