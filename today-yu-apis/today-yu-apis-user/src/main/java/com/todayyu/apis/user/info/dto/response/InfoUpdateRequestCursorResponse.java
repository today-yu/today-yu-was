package com.todayyu.apis.user.info.dto.response;

import com.todayyu.application.info.dto.InfoUpdateRequestCursor;

import java.time.LocalDateTime;

public record InfoUpdateRequestCursorResponse(
        LocalDateTime requestedAt,
        Long id
) {
    public static InfoUpdateRequestCursorResponse from(InfoUpdateRequestCursor cursor) {
        if (cursor == null) {
            return null;
        }
        
        return new InfoUpdateRequestCursorResponse(cursor.requestedAt(), cursor.id().getValue());
    }
}
