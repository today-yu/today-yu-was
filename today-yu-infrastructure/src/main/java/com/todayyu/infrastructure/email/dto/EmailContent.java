package com.todayyu.infrastructure.email.dto;

import lombok.Builder;
import org.springframework.core.io.ClassPathResource;

@Builder
public record EmailContent(
        String contentId,
        ClassPathResource content
) {
    public EmailContent(String contentId, String contentPath) {
        this(contentId, new ClassPathResource(contentPath));
    }
}
