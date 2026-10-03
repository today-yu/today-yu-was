package com.todayyu.infrastructure.jwt.properties;

import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.crypto.SecretKey;
import java.time.Duration;

@Getter
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {
    private final SecretKey secretKey;
    private final String issuer;
    private final Duration accessTokenExpiration;
    private final Duration refreshTokenExpiration;
    
    public JwtProperties(
            String key,
            String issuer,
            Duration accessTokenExpiration,
            Duration refreshTokenExpiration
    ) {
        this.secretKey = Keys.hmacShaKeyFor(Decoders.BASE64URL.decode(key));
        this.issuer = issuer;
        this.accessTokenExpiration = accessTokenExpiration;
        this.refreshTokenExpiration = refreshTokenExpiration;
    }
}
