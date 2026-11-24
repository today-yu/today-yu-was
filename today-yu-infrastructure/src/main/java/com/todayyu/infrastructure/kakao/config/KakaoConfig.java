package com.todayyu.infrastructure.kakao.config;

import com.todayyu.infrastructure.kakao.properties.KakaoProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(KakaoProperties.class)
public class KakaoConfig {
    
    @Bean
    public RestClient kakaoRestClient(RestClient.Builder builder, KakaoProperties kakaoProperties) {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(kakaoProperties.getConnectTimeout())
                .build();
        
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(kakaoProperties.getReadTimeout());
        
        return builder
                .requestFactory(requestFactory)
                .build();
    }
}
