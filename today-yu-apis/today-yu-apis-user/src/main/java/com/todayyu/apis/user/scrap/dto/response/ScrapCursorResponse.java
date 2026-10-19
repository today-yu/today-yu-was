package com.todayyu.apis.user.scrap.dto.response;

import com.todayyu.application.scrap.dto.ScrapCursor;

import java.time.LocalDateTime;

public record ScrapCursorResponse(
        LocalDateTime updatedAt,
        Long infoId
) {
    public static ScrapCursorResponse from(ScrapCursor cursor) {
        if (cursor == null) {
            return null;
        }
        
        return new ScrapCursorResponse(cursor.updatedAt(), cursor.infoId().getValue());
    }
}
