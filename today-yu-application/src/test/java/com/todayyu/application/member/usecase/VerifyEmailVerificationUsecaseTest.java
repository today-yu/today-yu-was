package com.todayyu.application.member.usecase;

import com.todayyu.application.member.dto.command.VerifyEmailVerificationCommand;
import com.todayyu.application.member.port.read.ReadEmailVerificationPort;
import com.todayyu.application.member.port.save.SaveEmailVerificationPort;
import com.todayyu.common.exception.CustomException;
import com.todayyu.domain.member.entity.EmailVerification;
import com.todayyu.domain.member.error.MemberErrorType;
import com.todayyu.domain.common.vo.Email;
import com.todayyu.domain.member.vo.VerificationCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class VerifyEmailVerificationUsecaseTest {
    private static final Email EMAIL = new Email("user@example.com");

    @Mock
    private ReadEmailVerificationPort readEmailVerificationPort;

    @Mock
    private SaveEmailVerificationPort saveEmailVerificationPort;

    @InjectMocks
    private VerifyEmailVerificationUsecase verifyEmailVerificationUsecase;

    @Test
    @DisplayName("인증 코드가 일치하면 인증에 성공하고 저장된 이메일 인증을 삭제한다")
    void verify() {
        EmailVerification existing = new EmailVerification(EMAIL, new VerificationCode("123456"), LocalDateTime.now());
        given(readEmailVerificationPort.readByEmail(EMAIL)).willReturn(Optional.of(existing));

        verifyEmailVerificationUsecase.verify(new VerifyEmailVerificationCommand(EMAIL, new VerificationCode("123456")));

        then(saveEmailVerificationPort).should().delete(existing);
    }

    @Test
    @DisplayName("발급된 인증 코드가 없으면 인증에 실패한다")
    void verifyWithoutIssuedCode() {
        given(readEmailVerificationPort.readByEmail(EMAIL)).willReturn(Optional.empty());
        VerifyEmailVerificationCommand command = new VerifyEmailVerificationCommand(EMAIL, new VerificationCode("123456"));

        CustomException exception = assertThrows(CustomException.class,
                () -> verifyEmailVerificationUsecase.verify(command));

        assertEquals(MemberErrorType.EMAIL_VERIFICATION_FAIL, exception.getErrorType());
        then(saveEmailVerificationPort).should(never()).delete(any());
    }

    @Test
    @DisplayName("인증 코드가 일치하지 않으면 인증에 실패하고 저장된 이메일 인증을 삭제하지 않는다")
    void verifyWithMismatchedCode() {
        EmailVerification existing = new EmailVerification(EMAIL, new VerificationCode("123456"), LocalDateTime.now());
        given(readEmailVerificationPort.readByEmail(EMAIL)).willReturn(Optional.of(existing));
        VerifyEmailVerificationCommand command = new VerifyEmailVerificationCommand(EMAIL, new VerificationCode("654321"));

        CustomException exception = assertThrows(CustomException.class,
                () -> verifyEmailVerificationUsecase.verify(command));

        assertEquals(MemberErrorType.EMAIL_VERIFICATION_FAIL, exception.getErrorType());
        then(saveEmailVerificationPort).should(never()).delete(any());
    }
}
