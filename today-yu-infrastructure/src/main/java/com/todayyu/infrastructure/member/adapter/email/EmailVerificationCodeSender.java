package com.todayyu.infrastructure.member.adapter.email;

import com.todayyu.application.member.port.VerificationCodeSender;
import com.todayyu.domain.member.entity.EmailVerification;
import com.todayyu.infrastructure.common.annotation.Adapter;
import com.todayyu.infrastructure.email.sender.EmailSender;
import com.todayyu.infrastructure.email.template.EmailTemplate;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;

import java.util.Map;

@Adapter
@RequiredArgsConstructor
public class EmailVerificationCodeSender implements VerificationCodeSender {
    private final EmailSender emailSender;
    
    private static final String CODE_CONTEXT_KEY = "code";
    private static final String DURATION_MINUTE_CONTEXT_KEY = "durationMinute";
    
    /**
     * 이메일을 통해 인증 코드를 발송하는 메서드
     *
     * <p> 인증 코드 전용 이메일 템플릿을 통해 이메일을 전송한다. </p>
     *
     * @param verification 이메일 인증 도메인 엔티티
     */
    @Async
    @Override
    public void send(EmailVerification verification) {
        emailSender.sendTemplateEmail(
                verification.getEmail().getValue(),
                EmailTemplate.VERIFICATION_CODE,
                Map.of(
                        CODE_CONTEXT_KEY, verification.getCode().getValue(),
                        DURATION_MINUTE_CONTEXT_KEY, String.valueOf(EmailVerification.EMAIL_VERIFICATION_DURATION_MINUTE)
                )
        );
    }
}
