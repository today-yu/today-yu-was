package com.todayyu.infrastructure.kakao.properties;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.Duration;

@Getter
@ConfigurationProperties(prefix = "kakao")
public class KakaoProperties {
    private final Duration connectTimeout;
    private final Duration readTimeout;
    private final String adminKey;
    private final String unlinkUrl;
    
    public KakaoProperties(
            @DefaultValue(value = "3s") Duration connectTimeout,
            @DefaultValue(value = "3s") Duration readTimeout,
            String adminKey,
            String unlinkUrl
    ) {
        this.connectTimeout = connectTimeout;
        this.readTimeout = readTimeout;
        this.adminKey = adminKey;
        this.unlinkUrl = unlinkUrl;
    }
}
