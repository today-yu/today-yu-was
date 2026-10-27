package com.todayyu.infrastructure.member.adapter.email;

import com.todayyu.domain.member.entity.EmailVerification;
import com.todayyu.domain.common.vo.Email;
import com.todayyu.domain.member.vo.VerificationCode;
import com.todayyu.infrastructure.email.sender.EmailSender;
import com.todayyu.infrastructure.email.template.EmailTemplate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class SmtpEmailVerificationSenderTest {
    private static final Email EMAIL = new Email("user@example.com");
    private static final VerificationCode CODE = new VerificationCode("123456");
    private static final LocalDateTime ISSUED_AT = LocalDateTime.of(2026, 9, 27, 10, 0);

    @Mock
    private EmailSender emailSender;

    @InjectMocks
    private SmtpEmailVerificationSender emailVerificationSender;

    @Captor
    private ArgumentCaptor<Map<String, String>> contextsCaptor;

    @Test
    @DisplayName("인증 코드 템플릿으로 이메일 인증의 이메일 주소에 인증 코드와 유효 시간을 담아 발송한다")
    void send() {
        EmailVerification verification = new EmailVerification(EMAIL, CODE, ISSUED_AT);

        emailVerificationSender.send(verification);

        then(emailSender).should().sendTemplateEmail(eq(EMAIL.getValue()), eq(EmailTemplate.EMAIL_VERIFICATION), contextsCaptor.capture());
        Map<String, String> contexts = contextsCaptor.getValue();
        assertEquals(CODE.getValue(), contexts.get("code"));
        assertEquals(String.valueOf(EmailVerification.EMAIL_VERIFICATION_DURATION_MINUTE), contexts.get("durationMinute"));
    }
}
