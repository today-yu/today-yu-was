package com.todayyu.infrastructure.email.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.mail.autoconfigure.MailProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Map;
import java.util.Properties;

@Slf4j
@Configuration
@EnableConfigurationProperties(value = MailProperties.class)
public class EmailConfig {
    @Bean
    public JavaMailSender javaMailSender(MailProperties mailProperties) {
        JavaMailSenderImpl javaMailSender = new JavaMailSenderImpl();
        javaMailSender.setHost(mailProperties.getHost());
        javaMailSender.setPort(mailProperties.getPort());
        javaMailSender.setUsername(mailProperties.getUsername());
        javaMailSender.setPassword(mailProperties.getPassword());
        javaMailSender.setJavaMailProperties(getPropertiesFromMailProperties(mailProperties));
        return javaMailSender;
    }

    private Properties getPropertiesFromMailProperties(MailProperties mailProperties) {
        Properties properties = new Properties();
        for (Map.Entry<String, String> entry : mailProperties.getProperties().entrySet()) {
            properties.setProperty(entry.getKey(), entry.getValue());
        }
        return properties;
    }
}
