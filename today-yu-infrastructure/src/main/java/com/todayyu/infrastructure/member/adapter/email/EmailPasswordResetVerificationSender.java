package com.todayyu.infrastructure.member.adapter.email;

import com.todayyu.application.member.port.SendPasswordResetVerificationPort;
import com.todayyu.domain.member.entity.PasswordResetVerification;
import com.todayyu.infrastructure.common.annotation.Adapter;
import com.todayyu.infrastructure.email.sender.EmailSender;
import com.todayyu.infrastructure.email.template.EmailTemplate;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;

import java.util.Map;

@Adapter
@RequiredArgsConstructor
public class EmailPasswordResetVerificationSender implements SendPasswordResetVerificationPort {
    private final EmailSender emailSender;
    
    private static final String CODE_CONTEXT_KEY = "code";
    private static final String DURATION_MINUTE_CONTEXT_KEY = "durationMinute";
    
    /**
     * 이메일을 통해 비밀번호 초기화 인증 코드를 발송하는 메서드
     *
     * <p> 비밀번호 초기화 전용 이메일 템플릿을 통해 이메일을 전송한다. </p>
     *
     * @param verification 비밀번호 초기화 인증 도메인 엔티티
     */
    @Async
    @Override
    public void send(PasswordResetVerification verification) {
        emailSender.sendTemplateEmail(
                verification.getEmail().getValue(),
                EmailTemplate.PASSWORD_RESET,
                Map.of(
                    CODE_CONTEXT_KEY, verification.getCode().getValue(),
                    DURATION_MINUTE_CONTEXT_KEY, String.valueOf(PasswordResetVerification.EXPIRATION_MINUTE)
                )
        );
    }
}
