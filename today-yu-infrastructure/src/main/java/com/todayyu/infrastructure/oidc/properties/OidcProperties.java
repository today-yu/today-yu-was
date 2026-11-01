package com.todayyu.infrastructure.oidc.properties;

import com.todayyu.domain.member.vo.AuthProvider;
import lombok.AccessLevel;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.Duration;
import java.util.Map;

@Getter
@ConfigurationProperties(prefix = "oidc")
public class OidcProperties {
    private final Duration connectTimeout;
    private final Duration readTimeout;
    
    @Getter(value = AccessLevel.NONE)
    private final Map<AuthProvider, Provider> providers;
    
    public OidcProperties(
            @DefaultValue(value = "3s") Duration connectTimeout,
            @DefaultValue(value = "3s") Duration readTimeout,
            Map<AuthProvider, Provider> providers
    ) {
        this.connectTimeout = connectTimeout;
        this.readTimeout = readTimeout;
        this.providers = providers;
    }
    
    public Provider getProvider(AuthProvider authProvider) {
        if (authProvider == null || !authProvider.isSocial()) {
            throw new IllegalArgumentException("소셜 인증 제공자가 아닙니다. authProvider = " + authProvider);
        }
        
        return providers.get(authProvider);
    }
    
    public record Provider(
            String clientId,
            String clientSecret,
            String redirectUri,
            String tokenUri,
            String issuer,
            String jwkSetUri
    ) { }
}
