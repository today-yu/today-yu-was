package com.todayyu.application.member.usecase;

import com.todayyu.application.common.annotation.Usecase;
import com.todayyu.application.member.dto.command.VerifyEmailVerificationCommand;
import com.todayyu.application.member.port.read.ReadEmailVerificationPort;
import com.todayyu.application.member.port.save.SaveEmailVerificationPort;
import com.todayyu.common.exception.CustomException;
import com.todayyu.domain.member.entity.EmailVerification;
import com.todayyu.domain.member.error.MemberErrorType;
import com.todayyu.domain.common.vo.Email;
import lombok.RequiredArgsConstructor;

@Usecase
@RequiredArgsConstructor
public class VerifyEmailVerificationUsecase {
    private final ReadEmailVerificationPort readEmailVerificationPort;
    private final SaveEmailVerificationPort saveEmailVerificationPort;
    
    /**
     * 사용자가 입력한 이메일 인증 코드와 저장된 인증 코드를 검증하는 메서드
     *
     * <p> 인증 코드가 일치할 경우, 기존에 저장된 인증 코드는 삭제한다. </p>
     *
     * @param command 이메일 인증 코드 검증 요청 DTO
     * @throws CustomException 인증 코드가 존재하지 않거나, 일치하지 않는 경우 - {@link MemberErrorType#EMAIL_VERIFICATION_FAIL}
     */
    public void verify(VerifyEmailVerificationCommand command) {
        Email email = command.email();
        
        EmailVerification verification = readEmailVerificationPort.readByEmail(email)
                .orElseThrow(() -> new CustomException(
                        MemberErrorType.EMAIL_VERIFICATION_FAIL,
                        "발급된 인증 코드가 존재하지 않습니다. email = " + email.getValue())
                );
        
        if (!verification.verify(command.code())) {
            throw new CustomException(
                    MemberErrorType.EMAIL_VERIFICATION_FAIL,
                    "인증 코드가 일치하지 않습니다. email = " + email.getValue()
            );
        }
        
        saveEmailVerificationPort.delete(verification);
    }
}
