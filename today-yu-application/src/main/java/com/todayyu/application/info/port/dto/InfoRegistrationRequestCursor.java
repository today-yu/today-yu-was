package com.todayyu.application.info.port.dto;

import com.todayyu.domain.info.vo.InfoRegistrationRequestId;

import java.time.LocalDateTime;

public record InfoRegistrationRequestCursor(
        LocalDateTime requestedAt,
        InfoRegistrationRequestId id
) {
    public InfoRegistrationRequestCursor {
        if (requestedAt == null || id == null) {
            throw new IllegalArgumentException("커서 값은 비어있을 수 없습니다.");
        }
    }
    
    public static InfoRegistrationRequestCursor from(InfoRegistrationRequestSummaryView view) {
        return new InfoRegistrationRequestCursor(view.requestedAt(), view.id());
    }
}
