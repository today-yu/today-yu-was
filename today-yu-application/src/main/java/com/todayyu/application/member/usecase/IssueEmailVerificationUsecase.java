package com.todayyu.application.member.usecase;

import com.todayyu.application.common.annotation.Usecase;
import com.todayyu.application.member.dto.command.IssueEmailVerificationCommand;
import com.todayyu.application.member.port.VerificationCodeSender;
import com.todayyu.application.member.port.read.ReadEmailVerificationPort;
import com.todayyu.application.member.port.save.SaveEmailVerificationPort;
import com.todayyu.application.member.util.VerificationCodeGenerator;
import com.todayyu.domain.member.entity.EmailVerification;
import com.todayyu.domain.common.vo.Email;
import com.todayyu.domain.member.vo.VerificationCode;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.Optional;

@Usecase
@RequiredArgsConstructor
public class IssueEmailVerificationUsecase {
    private final ReadEmailVerificationPort readEmailVerificationPort;
    private final SaveEmailVerificationPort saveEmailVerificationPort;
    private final VerificationCodeSender verificationCodeSender;
    
    /**
     * 이메일 인증 코드 발급 메서드
     *
     * <p> 기존 이메일 인증 코드 발급 이력이 존재하는 경우, {@link EmailVerification#reissue(VerificationCode, LocalDateTime)}를
     *     호출하여 이메일 인증 객체를 생성하며, 재발급 제한 시간일 경우 예외를 발생한다.</p>
     *
     * @param command 이메일 인증 코드 발급 요청 DTO
     */
    public void issue(IssueEmailVerificationCommand command) {
        Email email = command.email();
        LocalDateTime now = LocalDateTime.now();
        VerificationCode verificationCode = new VerificationCode(VerificationCodeGenerator.generate());
        
        Optional<EmailVerification> existedVerification = readEmailVerificationPort.readByEmail(email);
        EmailVerification newVerification = null;
        
        if (existedVerification.isPresent()) {
            newVerification = existedVerification.get().reissue(verificationCode, now);
        } else {
            newVerification = new EmailVerification(email, verificationCode, now);
        }
        
        saveEmailVerificationPort.save(newVerification);
        verificationCodeSender.send(newVerification);
    }
}
