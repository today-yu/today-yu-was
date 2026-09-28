package com.todayyu.infrastructure.email.template;

import com.todayyu.infrastructure.email.dto.EmailContent;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor
public enum EmailTemplate {
    VERIFICATION_CODE(
            "mail/verification-code",
            "[오늘의 영대] 이메일 인증 코드",
            List.of(
                    new EmailContent("logo", "mail/images/logo.png"),
                    new EmailContent("header-decoration", "mail/images/header-decoration.png"),
                    new EmailContent("clock", "mail/images/clock.png")
            )
    ),
    ;
    
    private final String templatePath;
    private final String subject;
    private final List<EmailContent> contents;
}
