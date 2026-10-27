package com.todayyu.infrastructure.email.sender;

import com.todayyu.infrastructure.email.exception.EmailSendFailException;
import com.todayyu.infrastructure.email.template.EmailTemplate;
import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.mail.autoconfigure.MailProperties;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.javamail.JavaMailSender;
import org.thymeleaf.context.IContext;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;

@ExtendWith(MockitoExtension.class)
class EmailSenderTest {
    private static final String FROM = "sender@example.com";
    private static final String TO = "user@example.com";
    private static final EmailTemplate TEMPLATE = EmailTemplate.EMAIL_VERIFICATION;

    @Mock
    private JavaMailSender javaMailSender;

    @Mock
    private SpringTemplateEngine templateEngine;

    @Captor
    private ArgumentCaptor<IContext> contextCaptor;

    @Captor
    private ArgumentCaptor<MimeMessage> messageCaptor;

    private EmailSender emailSender;

    @BeforeEach
    void setUp() {
        MailProperties mailProperties = new MailProperties();
        mailProperties.setUsername(FROM);
        emailSender = new EmailSender(javaMailSender, mailProperties, templateEngine);

        given(templateEngine.process(eq(TEMPLATE.getTemplatePath()), any(IContext.class))).willReturn("<p>html</p>");
        given(javaMailSender.createMimeMessage()).willReturn(new MimeMessage((Session) null));
    }

    @Test
    @DisplayName("템플릿 변수로 렌더링한 메일을 발신자, 수신자, 템플릿 제목으로 발송한다")
    void sendTemplateEmail() throws Exception {
        emailSender.sendTemplateEmail(TO, TEMPLATE, Map.of("code", "123456"));

        then(templateEngine).should().process(eq(TEMPLATE.getTemplatePath()), contextCaptor.capture());
        assertEquals("123456", contextCaptor.getValue().getVariable("code"));

        then(javaMailSender).should().send(messageCaptor.capture());
        MimeMessage message = messageCaptor.getValue();
        assertEquals(FROM, ((InternetAddress) message.getFrom()[0]).getAddress());
        assertEquals(TO, ((InternetAddress) message.getAllRecipients()[0]).getAddress());
        assertEquals(TEMPLATE.getSubject(), message.getSubject());
    }

    @Test
    @DisplayName("메일 발송에 실패하면 EmailSendFailException으로 감싸서 던진다")
    void sendFail() {
        MailAuthenticationException cause = new MailAuthenticationException("Authentication failed");
        willThrow(cause).given(javaMailSender).send(any(MimeMessage.class));

        EmailSendFailException exception = assertThrows(EmailSendFailException.class,
                () -> emailSender.sendTemplateEmail(TO, TEMPLATE, Map.of("code", "123456")));

        assertEquals(TO, exception.getTo());
        assertEquals(TEMPLATE.getSubject(), exception.getSubject());
        assertInstanceOf(MailAuthenticationException.class, exception.getCause());
    }
}
