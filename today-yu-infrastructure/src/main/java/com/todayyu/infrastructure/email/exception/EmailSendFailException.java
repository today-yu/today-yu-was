package com.todayyu.infrastructure.email.exception;

import lombok.Getter;

@Getter
public class EmailSendFailException extends RuntimeException {
    private String to;
    private String subject;
    
    public EmailSendFailException(String to, String subject, Throwable e) {
        super("메일 발송에 실패하였습니다. - (to: %s, subject: %s)".formatted(to, subject), e);
        this.to = to;
        this.subject = subject;
    }
}
