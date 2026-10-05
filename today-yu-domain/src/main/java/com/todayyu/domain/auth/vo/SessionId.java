package com.todayyu.domain.auth.vo;

import lombok.Getter;

import java.util.Objects;
import java.util.UUID;

@Getter
public class SessionId {
    private final String value;
    
    private SessionId(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("세션 ID는 비어있을 수 없습니다.");
        }
        
        this.value = value;
    }
    
    public static SessionId create() {
        return new SessionId(UUID.randomUUID().toString());
    }
    
    public static SessionId from(String sessionId) {
        return new SessionId(sessionId);
    }
    
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof SessionId sessionId)) return false;
        return Objects.equals(value, sessionId.value);
    }
    
    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }
}
