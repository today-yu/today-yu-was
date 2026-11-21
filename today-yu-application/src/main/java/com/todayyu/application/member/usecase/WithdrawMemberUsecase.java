package com.todayyu.application.member.usecase;

import com.todayyu.application.common.annotation.Usecase;
import com.todayyu.application.member.dto.command.WithdrawMemberCommand;
import com.todayyu.application.member.error.BusinessMemberErrorType;
import com.todayyu.application.member.event.MemberWithdrawalEvent;
import com.todayyu.application.member.port.PasswordPort;
import com.todayyu.application.member.port.read.ReadMemberPort;
import com.todayyu.application.member.port.save.SaveMemberPort;
import com.todayyu.common.exception.CustomException;
import com.todayyu.domain.member.entity.LocalAccount;
import com.todayyu.domain.member.entity.Member;
import com.todayyu.domain.member.vo.EncodedPassword;
import com.todayyu.domain.member.vo.MemberId;
import com.todayyu.domain.member.vo.Password;
import com.todayyu.domain.member.vo.SocialIdentity;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Usecase
@RequiredArgsConstructor
public class WithdrawMemberUsecase {
    private final ReadMemberPort readMemberPort;
    private final PasswordPort passwordPort;
    private final SaveMemberPort saveMemberPort;
    private final ApplicationEventPublisher eventPublisher;
    
    /**
     * 회원 탈퇴 메서드
     *
     * <p> {@link #validateLocalAccount(Member, Password)}를 호출하여 일반 로그인 계정({@link LocalAccount})이 존재하는
     *     경우 비밀번호 일치 여부를 확인한다. </p>
     *
     * <p> 회원과 연동된 계정을 삭제하고, 기존 회원 정보는 비식별화하여 탈퇴 처리한다. </p>
     *
     * <p> 회원 연동 계정 삭제 전, 소셜 로그인 인증 정보를 저장한다. </p>
     *
     * <p> 탈퇴 후 {@link MemberWithdrawalEvent}를 발행하며, 다음 작업이 처리된다. </p>
     * <ul>
     *     <li> 같은 트랜잭션: 정보 등록/수정 요청 삭제 및 비식별화, 문의·스크랩·도움이 되는 정보에요 등 삭제 </li>
     *     <li> 커밋 이후: Refresh Token 삭제, 소셜 로그인 서비스 연동 해제 </li>
     * </ul>
     *
     * @throws CustomException
     * <ul>
     *      <li> {@link BusinessMemberErrorType#NOT_FOUND} - 회원이 존재하지 않거나 이미 탈퇴한 경우 </li>
     *      <li> {@link BusinessMemberErrorType#PASSWORD_REQUIRED} - 로컬 회원이 비밀번호를 입력하지 않은 경우 </li>
     *      <li> {@link BusinessMemberErrorType#CURRENT_PASSWORD_MISMATCH} - 비밀번호가 일치하지 않는 경우 </li>
     * </ul>
     */
    @Transactional
    public void withdraw(WithdrawMemberCommand command) {
        MemberId memberId = command.memberId();
        Password password = command.password();
        
        Member member = readMemberPort.readByMemberId(memberId)
                .orElseThrow(() -> new CustomException(BusinessMemberErrorType.NOT_FOUND));
        
        validateLocalAccount(member, password);
        
        List<SocialIdentity> socialIdentities = member.getSocialIdentities();
        member.withdraw(LocalDateTime.now());
        
        saveMemberPort.withdraw(member);
        eventPublisher.publishEvent(new MemberWithdrawalEvent(memberId, socialIdentities));
    }
    
    /**
     * 일반 로그인 계정의 회원 탈퇴를 위한 유효성 검증 메서드
     *
     * <p> {@link Member#getEncodedPassword()}가 비어있을 경우, 소셜 로그인 계정만 보유한 회원이므로 검증을 더 이상
     *     진행하지 않는다.</p>
     *
     * <p> 일반 로그인 사용자는 비밀번호가 일치하지 않는 경우 예외가 발생한다.  </p>
     *
     * @param member 회원 객체
     * @param password 비밀번호
     * @throws CustomException
     * <ul>
     *     <li> {@link BusinessMemberErrorType#PASSWORD_REQUIRED} - 로컬 계정이지만 비밀번호가 비어있는 경우 </li>
     *     <li> {@link BusinessMemberErrorType#CURRENT_PASSWORD_MISMATCH} - 비밀번호가 일치하지 않는 경우 </li>
     * </ul>
     */
    private void validateLocalAccount(Member member, Password password) {
        Optional<EncodedPassword> encodedPassword = member.getEncodedPassword();
        
        if (encodedPassword.isEmpty()) {
            return;
        }
        
        if (password == null) {
            throw new CustomException(BusinessMemberErrorType.PASSWORD_REQUIRED);
        }
        
        if (!passwordPort.matches(password, encodedPassword.get())) {
            throw new CustomException(BusinessMemberErrorType.CURRENT_PASSWORD_MISMATCH);
        }
    }
}
