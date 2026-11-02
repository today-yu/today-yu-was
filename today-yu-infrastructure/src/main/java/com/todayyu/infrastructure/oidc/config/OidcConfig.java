package com.todayyu.infrastructure.oidc.config;

import com.todayyu.infrastructure.oidc.properties.OidcProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(OidcProperties.class)
public class OidcConfig {
    
    @Bean
    public RestClient oidcRestClient(RestClient.Builder builder, OidcProperties oidcProperties) {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(oidcProperties.getConnectTimeout())
                .build();

        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(oidcProperties.getReadTimeout());

        return builder
                .requestFactory(requestFactory)
                .build();
    }
}