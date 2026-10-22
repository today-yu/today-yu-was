package com.todayyu.application.member.usecase;

import com.todayyu.application.common.annotation.Usecase;
import com.todayyu.application.member.dto.command.IssuePasswordResetVerificationCommand;
import com.todayyu.application.member.port.read.ReadMemberPort;
import com.todayyu.application.member.port.read.ReadPasswordResetVerificationPort;
import com.todayyu.application.member.port.save.SavePasswordResetVerificationPort;
import com.todayyu.application.member.util.VerificationCodeGenerator;
import com.todayyu.common.exception.CustomException;
import com.todayyu.domain.common.vo.Email;
import com.todayyu.domain.member.entity.Member;
import com.todayyu.domain.member.entity.PasswordResetVerification;
import com.todayyu.domain.member.error.MemberErrorType;
import com.todayyu.domain.member.vo.VerificationCode;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Usecase
@RequiredArgsConstructor
public class IssuePasswordResetVerificationUsecase {
    private final ReadPasswordResetVerificationPort readPasswordResetVerificationPort;
    private final SavePasswordResetVerificationPort savePasswordResetVerificationPort;
    private final ReadMemberPort readMemberPort;
    
    /**
     * 비밀번호 재설정 인증 코드 발급 메서드
     *
     * <p> 가입 여부 노출을 막기 위해, 해당 이메일로 가입된 로컬 계정이 없으면 예외 없이 발급을 생략한다. </p>
     *
     * <p> 기존 인증이 존재하는 경우 {@link PasswordResetVerification#reissue(VerificationCode, LocalDateTime)}로 재발급한다. </p>
     *
     * @param command 비밀번호 재설정 인증 코드 발급 요청 DTO
     * @throws CustomException {@link MemberErrorType#VERIFICATION_CODE_REISSUE_TOO_EARLY} - 재발급 가능 시각 이전에 요청한 경우
     */
    @Transactional(readOnly = true)
    public void issue(IssuePasswordResetVerificationCommand command) {
        Email email = command.email();
        
        boolean hasLocalAccount = readMemberPort.readByEmail(email)
                .map(Member::hasLocalAccount)
                .orElse(false);
        
        if (!hasLocalAccount) {
            return;
        }
        
        LocalDateTime now = LocalDateTime.now();
        VerificationCode code = new VerificationCode(VerificationCodeGenerator.generate());
        
        PasswordResetVerification verification = readPasswordResetVerificationPort.readByEmail(email)
                .map(existedVerification -> existedVerification.reissue(code, now))
                .orElseGet(() -> PasswordResetVerification.issue(email, code, now));
        
        savePasswordResetVerificationPort.save(verification);
        
        // TODO: 메일 발송
    }
}
