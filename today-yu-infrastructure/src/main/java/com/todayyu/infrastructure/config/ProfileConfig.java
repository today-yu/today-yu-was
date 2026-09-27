package com.todayyu.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

public class ProfileConfig {
    @Configuration
    @PropertySource(value = {"classpath:/infrastructure/property-source-default.properties"})
    public static class DefaultConfig { }
}
