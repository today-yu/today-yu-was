package com.todayyu.application.scrap.listener;

import com.todayyu.application.member.event.MemberWithdrawalEvent;
import com.todayyu.application.scrap.port.save.SaveScrapPort;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.IllegalTransactionStateException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class ScrapMemberWithdrawalListener {
    private final SaveScrapPort saveScrapPort;

    /**
     * 탈퇴한 회원의 스크랩을 모두 삭제하는 탈퇴 회원 이벤트 리스너 메서드
     *
     * <p> 정보와 관계없이 해당 회원의 스크랩을 모두 삭제한다.
     *
     * <p> 회원 탈퇴와 함께 성공·실패해야 하므로, 회원 탈퇴 트랜잭션 안에서만 실행된다.
     *     처리 중 예외가 발생하면 회원 탈퇴도 함께 롤백된다. </p>
     *
     * @param event 회원 탈퇴 이벤트
     * @throws IllegalTransactionStateException 진행 중인 트랜잭션 없이 호출된 경우
     */
    @Transactional(propagation = Propagation.MANDATORY)
    @EventListener
    public void deleteScraps(MemberWithdrawalEvent event) {
        saveScrapPort.deleteAllByMemberId(event.memberId());
    }
}
