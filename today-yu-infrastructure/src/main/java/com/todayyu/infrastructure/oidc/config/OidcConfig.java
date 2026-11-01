package com.todayyu.infrastructure.oidc.config;

import com.todayyu.infrastructure.oidc.properties.OidcProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(OidcProperties.class)
public class OidcConfig {
}
