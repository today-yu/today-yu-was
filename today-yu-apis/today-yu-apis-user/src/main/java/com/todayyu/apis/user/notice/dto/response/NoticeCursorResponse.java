package com.todayyu.apis.user.notice.dto.response;

import com.todayyu.application.notice.dto.NoticeCursor;

public record NoticeCursorResponse(
        Long id
) {
    public static NoticeCursorResponse from(NoticeCursor cursor) {
        if (cursor == null) {
            return null;
        }
        
        return new NoticeCursorResponse(cursor.noticeId().getValue());
    }
}
