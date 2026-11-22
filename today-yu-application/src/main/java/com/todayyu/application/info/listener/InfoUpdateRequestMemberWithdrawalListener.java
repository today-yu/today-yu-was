package com.todayyu.application.info.listener;

import com.todayyu.application.info.port.read.ReadInfoUpdateRequestPort;
import com.todayyu.application.info.port.save.SaveInfoUpdateRequestPort;
import com.todayyu.application.member.event.MemberWithdrawalEvent;
import com.todayyu.domain.info.entity.InfoUpdateRequest;
import com.todayyu.domain.info.vo.InfoRequestStatus;
import com.todayyu.domain.info.vo.InfoUpdateRequestId;
import com.todayyu.domain.member.vo.MemberId;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.IllegalTransactionStateException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class InfoUpdateRequestMemberWithdrawalListener {
    private final ReadInfoUpdateRequestPort readInfoUpdateRequestPort;
    private final SaveInfoUpdateRequestPort saveInfoUpdateRequestPort;

    /**
     * 탈퇴한 회원의 정보 수정 요청을 상태에 따라 삭제하거나 비식별화하는 탈퇴 회원 이벤트 리스너 메서드
     *
     * <ul>
     *     <li> {@link InfoRequestStatus#PENDING}, {@link InfoRequestStatus#REJECTED} - 요청을 삭제한다. </li>
     *     <li> {@link InfoRequestStatus#ACCEPTED} - 연락 이메일만 제거(비식별화)하여 보관한다. </li>
     * </ul>
     *
     * <p> 회원 탈퇴와 함께 성공·실패해야 하므로, 회원 탈퇴 트랜잭션 안에서만 실행된다.
     *     처리 중 예외가 발생하면 회원 탈퇴도 함께 롤백된다. </p>
     *
     * @param event 회원 탈퇴 이벤트
     * @throws IllegalTransactionStateException 진행 중인 트랜잭션 없이 호출된 경우
     */
    @Transactional(propagation = Propagation.MANDATORY)
    @EventListener
    public void deleteOrAnonymizeRequests(MemberWithdrawalEvent event) {
        MemberId memberId = event.memberId();

        List<InfoUpdateRequestId> requestIdsToDelete = new ArrayList<>();
        List<InfoUpdateRequest> requestsToAnonymize = new ArrayList<>();

        readInfoUpdateRequestPort.readAllByMemberId(memberId).stream()
                .forEach(request -> {
                    switch (request.getStatus()) {
                        case PENDING, REJECTED -> requestIdsToDelete.add(request.getId());
                        case ACCEPTED -> {
                            request.anonymize();
                            requestsToAnonymize.add(request);
                        }
                    }
                });

        saveInfoUpdateRequestPort.deleteByIds(requestIdsToDelete);
        saveInfoUpdateRequestPort.anonymizeAll(requestsToAnonymize);
    }
}
