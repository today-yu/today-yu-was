package com.todayyu.application.member.usecase;

import com.todayyu.application.common.annotation.Usecase;
import com.todayyu.application.member.dto.VerificationCodeIssueRequest;
import com.todayyu.application.member.port.read.ReadEmailVerificationPort;
import com.todayyu.application.member.port.save.SaveEmailVerificationPort;
import com.todayyu.application.member.util.VerificationCodeGenerator;
import com.todayyu.domain.member.entity.EmailVerification;
import com.todayyu.domain.member.vo.Email;
import com.todayyu.domain.member.vo.VerificationCode;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.Optional;


@Usecase
@RequiredArgsConstructor
public class EmailVerificationUsecase {
    private final ReadEmailVerificationPort readEmailVerificationPort;
    private final SaveEmailVerificationPort saveEmailVerificationPort;
    
    /**
     * 이메일 인증 코드 발급 메서드
     *
     * <p> 기존 이메일 인증 코드 발급 이력이 존재하는 경우, {@link EmailVerification#reissue(VerificationCode, LocalDateTime)}를
     *     호출하여 이메일 인증 객체를 생성하며, 재발급 제한 시간일 경우 예외를 발생한다.</p>
     *
     * @param request 이메일 인증 코드 발급 요청 DTO
     */
    public void issueVerificationCode(VerificationCodeIssueRequest request) {
        Email email = new Email(request.email());
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
        // TODO: 이메일 인증 코드 발송 로직 추가
    }
}
