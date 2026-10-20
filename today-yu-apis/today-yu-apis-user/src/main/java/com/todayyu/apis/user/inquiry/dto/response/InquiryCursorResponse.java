package com.todayyu.apis.user.inquiry.dto.response;

import com.todayyu.application.inquiry.dto.InquiryCursor;

public record InquiryCursorResponse(
        Long id
) {
    public static InquiryCursorResponse from(InquiryCursor cursor) {
        return new InquiryCursorResponse(cursor.id().getValue());
    }
}
