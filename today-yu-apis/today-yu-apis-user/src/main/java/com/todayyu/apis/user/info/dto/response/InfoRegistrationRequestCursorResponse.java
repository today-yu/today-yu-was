package com.todayyu.apis.user.info.dto.response;

import com.todayyu.application.info.port.dto.InfoRegistrationRequestCursor;

import java.time.LocalDateTime;

public record InfoRegistrationRequestCursorResponse(
        LocalDateTime requestedAt,
        Long id
) {
    public static InfoRegistrationRequestCursorResponse from(InfoRegistrationRequestCursor cursor) {
        if (cursor == null) {
            return null;
        }
        
        return new InfoRegistrationRequestCursorResponse(cursor.requestedAt(), cursor.id().getValue());
    }
}
