package com.todayyu.domain.common.event;

import lombok.Getter;

import java.util.Objects;
import java.util.UUID;

@Getter
public class EventId {
    private final String value;
    
    private EventId(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("이벤트 아이디는 비어있을 수 없습니다.");
        }
        
        this.value = value;
    }
    
    public static EventId generate() {
        return new EventId(UUID.randomUUID().toString());
    }
    
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof EventId eventId)) return false;
        return Objects.equals(value, eventId.value);
    }
    
    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }
    
    @Override
    public String toString() {
        return value;
    }
}
