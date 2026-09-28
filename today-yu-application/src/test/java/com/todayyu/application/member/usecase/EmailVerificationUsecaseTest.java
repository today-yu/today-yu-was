package com.todayyu.application.member.usecase;

import com.todayyu.application.member.dto.VerificationCodeIssueCommand;
import com.todayyu.application.member.port.VerificationCodeSender;
import com.todayyu.application.member.port.read.ReadEmailVerificationPort;
import com.todayyu.application.member.port.save.SaveEmailVerificationPort;
import com.todayyu.common.exception.CustomException;
import com.todayyu.domain.member.entity.EmailVerification;
import com.todayyu.domain.member.error.MemberErrorType;
import com.todayyu.domain.member.vo.Email;
import com.todayyu.domain.member.vo.VerificationCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class EmailVerificationUsecaseTest {
    private static final Email EMAIL = new Email("user@example.com");
    private static final VerificationCodeIssueCommand COMMAND = new VerificationCodeIssueCommand(EMAIL.getValue());

    @Mock
    private ReadEmailVerificationPort readEmailVerificationPort;

    @Mock
    private SaveEmailVerificationPort saveEmailVerificationPort;

    @Mock
    private VerificationCodeSender verificationCodeSender;

    @InjectMocks
    private EmailVerificationUsecase emailVerificationUsecase;

    @Captor
    private ArgumentCaptor<EmailVerification> verificationCaptor;

    @Test
    @DisplayName("진행 중인 이메일 인증이 없으면 6자리 인증 코드를 새로 발급해 저장한다")
    void issue() {
        given(readEmailVerificationPort.readByEmail(EMAIL)).willReturn(Optional.empty());

        emailVerificationUsecase.issueVerificationCode(COMMAND);

        then(saveEmailVerificationPort).should().save(verificationCaptor.capture());
        EmailVerification saved = verificationCaptor.getValue();
        assertEquals(EMAIL, saved.getEmail());
        assertTrue(saved.getCode().getValue().matches("\\d{6}"));
    }

    @Test
    @DisplayName("발급 후 1분이 지났다면 인증 코드를 재발급해 저장한다")
    void reissue() {
        LocalDateTime previousIssuedAt = LocalDateTime.now().minusMinutes(2);
        EmailVerification existing = new EmailVerification(EMAIL, new VerificationCode("111111"), previousIssuedAt);
        given(readEmailVerificationPort.readByEmail(EMAIL)).willReturn(Optional.of(existing));

        emailVerificationUsecase.issueVerificationCode(COMMAND);

        then(saveEmailVerificationPort).should().save(verificationCaptor.capture());
        EmailVerification saved = verificationCaptor.getValue();
        assertEquals(EMAIL, saved.getEmail());
        assertTrue(saved.getIssuedAt().isAfter(previousIssuedAt));
    }

    @Test
    @DisplayName("발급 후 1분이 지나지 않았다면 재발급할 수 없고 저장하지 않는다")
    void reissueTooEarly() {
        EmailVerification existing = new EmailVerification(EMAIL, new VerificationCode("111111"), LocalDateTime.now());
        given(readEmailVerificationPort.readByEmail(EMAIL)).willReturn(Optional.of(existing));

        CustomException exception = assertThrows(CustomException.class,
                () -> emailVerificationUsecase.issueVerificationCode(COMMAND));

        assertEquals(MemberErrorType.VERIFICATION_CODE_REISSUE_TOO_EARLY, exception.getErrorType());
        then(saveEmailVerificationPort).should(never()).save(any());
        then(verificationCodeSender).should(never()).send(any());
    }

    @Test
    @DisplayName("인증 코드를 저장한 뒤, 저장한 이메일 인증으로 인증 코드를 발송한다")
    void sendAfterSave() {
        given(readEmailVerificationPort.readByEmail(EMAIL)).willReturn(Optional.empty());

        emailVerificationUsecase.issueVerificationCode(COMMAND);

        InOrder inOrder = inOrder(saveEmailVerificationPort, verificationCodeSender);
        inOrder.verify(saveEmailVerificationPort).save(verificationCaptor.capture());
        EmailVerification saved = verificationCaptor.getValue();
        inOrder.verify(verificationCodeSender).send(verificationCaptor.capture());
        assertSame(saved, verificationCaptor.getValue());
    }

    @Test
    @DisplayName("재발급한 경우에도 새로 발급한 인증 코드로 발송한다")
    void sendReissuedCode() {
        LocalDateTime previousIssuedAt = LocalDateTime.now().minusMinutes(2);
        EmailVerification existing = new EmailVerification(EMAIL, new VerificationCode("111111"), previousIssuedAt);
        given(readEmailVerificationPort.readByEmail(EMAIL)).willReturn(Optional.of(existing));

        emailVerificationUsecase.issueVerificationCode(COMMAND);

        then(verificationCodeSender).should().send(verificationCaptor.capture());
        EmailVerification sent = verificationCaptor.getValue();
        assertEquals(EMAIL, sent.getEmail());
        assertTrue(sent.getIssuedAt().isAfter(previousIssuedAt));
    }

    @Test
    @DisplayName("인증 코드 저장에 실패하면 인증 코드를 발송하지 않는다")
    void notSendWhenSaveFails() {
        given(readEmailVerificationPort.readByEmail(EMAIL)).willReturn(Optional.empty());
        willThrow(new RuntimeException("save failed")).given(saveEmailVerificationPort).save(any());

        assertThrows(RuntimeException.class, () -> emailVerificationUsecase.issueVerificationCode(COMMAND));

        then(verificationCodeSender).should(never()).send(any());
    }
}
