package com.todayyu.application.member.usecase;

import com.todayyu.application.common.annotation.Usecase;
import com.todayyu.application.member.dto.VerificationCodeIssueCommand;
import com.todayyu.application.member.dto.VerificationCodeVerifyCommand;
import com.todayyu.application.member.port.VerificationCodeSender;
import com.todayyu.application.member.port.read.ReadEmailVerificationPort;
import com.todayyu.application.member.port.save.SaveEmailVerificationPort;
import com.todayyu.application.member.util.VerificationCodeGenerator;
import com.todayyu.common.exception.CustomException;
import com.todayyu.domain.member.entity.EmailVerification;
import com.todayyu.domain.member.error.MemberErrorType;
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
    private final VerificationCodeSender verificationCodeSender;
    
    /**
     * 이메일 인증 코드 발급 메서드
     *
     * <p> 기존 이메일 인증 코드 발급 이력이 존재하는 경우, {@link EmailVerification#reissue(VerificationCode, LocalDateTime)}를
     *     호출하여 이메일 인증 객체를 생성하며, 재발급 제한 시간일 경우 예외를 발생한다.</p>
     *
     * @param command 이메일 인증 코드 발급 요청 DTO
     */
    public void issueVerificationCode(VerificationCodeIssueCommand command) {
        Email email = new Email(command.email());
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
    
    /**
     * 사용자가 입력한 이메일 인증 코드와 저장된 인증 코드를 검증하는 메서드
     *
     * <p> 인증 코드가 일치할 경우, 기존에 저장된 인증 코드는 삭제한다. </p>
     *
     * @param command 이메일 인증 코드 검증 요청 DTO
     * @throws CustomException 인증 코드가 존재하지 않거나, 일치하지 않는 경우 - {@link MemberErrorType#EMAIL_VERIFICATION_FAIL}
     */
    public void verify(VerificationCodeVerifyCommand command) {
        Email email = new Email(command.email());
        VerificationCode code = new VerificationCode(command.code());
        
        EmailVerification verification = readEmailVerificationPort.readByEmail(email)
                .orElseThrow(() -> new CustomException(
                        MemberErrorType.EMAIL_VERIFICATION_FAIL,
                        "발급된 인증 코드가 존재하지 않습니다. email = " + command.email())
                );
        
        if (!verification.verify(code)) {
            throw new CustomException(
                    MemberErrorType.EMAIL_VERIFICATION_FAIL,
                    "인증 코드가 일치하지 않습니다. email = " + command.email()
            );
        }
        
        saveEmailVerificationPort.delete(verification);
    }
}
