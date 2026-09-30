package com.todayyu.persistence.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;

public class PersistenceProfileConfig {
    @Configuration
    @PropertySource(value = {"classpath:/persistence/property-source-default.properties"})
    public static class DefaultConfig { }
    
    @Configuration
    @Profile("develop")
    @PropertySource(value = {"classpath:/persistence/property-source-develop.properties"})
    public static class DevelopConfig { }
    
    @Configuration
    @Profile("production")
    @PropertySource(value = {"classpath:/persistence/property-source-production.properties"})
    public static class ProductionConfig { }
}
