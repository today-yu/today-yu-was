package com.todayyu.infrastructure.email.sender;

import com.todayyu.infrastructure.email.dto.EmailContent;
import com.todayyu.infrastructure.email.exception.EmailSendFailException;
import com.todayyu.infrastructure.email.template.EmailTemplate;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.mail.autoconfigure.MailProperties;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class EmailSender {
    private final JavaMailSender javaMailSender;
    private final MailProperties mailProperties;
    private final SpringTemplateEngine templateEngine;
    
    /**
     * 템플릿을 가지는 이메일을 발송하는 메서드
     *
     * @param to 수신자 이메일
     * @param template 이메일 템플릿 정보
     * @param contexts 타임리프 컨텍스트({@link Context})를 통해 이메일에 전달할 값
     */
    public void sendTemplateEmail(String to, EmailTemplate template, Map<String, String> contexts) {
        Context context = new Context();
        setContext(context, contexts);
        
        String html = templateEngine.process(template.getTemplatePath(), context);
        
        MimeMessage message = javaMailSender.createMimeMessage();
        
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        
            helper.setTo(to);
            helper.setSubject(template.getSubject());
            helper.setFrom(mailProperties.getUsername());
            helper.setText(html, true);
            setContent(helper, template.getContents());
            
            javaMailSender.send(message);
        } catch (MessagingException | MailException e) {
            throw new EmailSendFailException(to, template.getSubject(), e);
        }
    }
    
    /**
     * 타임리프 컨텍스트({@link Context}) 객체에 사용할 컨텍스트를 추가하는 메서드
     *
     * @param context 타임리프 컨텍스트
     * @param contexts 추가할 컨텍스트
     */
    private void setContext(Context context, Map<String, String> contexts) {
        for (Map.Entry<String, String> c : contexts.entrySet()) {
            context.setVariable(c.getKey(), c.getValue());
        }
    }
    
    /**
     * 타임리프 템플릿에 사용할 컨텐츠(이미지 등)의 경로를 삽입하는 메서드
     *
     * @param helper {@link MimeMessageHelper} 객체
     * @param contents 템플릿에 사용하는 컨텐츠와 컨텐츠 이미지 경로 목록
     * @throws MessagingException 컨텐츠 추가 중 에러가 발생한 경우
     */
    private void setContent(MimeMessageHelper helper, List<EmailContent> contents) throws MessagingException {
        for (EmailContent content : contents) {
            helper.addInline(content.contentId(), content.content());
        }
    }
}
