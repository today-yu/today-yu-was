package com.todayyu.apis.user.info.dto.response;

import com.todayyu.application.info.dto.InfoCursor;

import java.time.LocalDateTime;

public record InfoCursorResponse(
        LocalDateTime updatedAt,
        Long infoId
) {
    public static InfoCursorResponse from(InfoCursor cursor) {
        if (cursor == null) {
            return null;
        }
        
        return new InfoCursorResponse(cursor.updatedAt(), cursor.infoId().getValue());
    }
}
