package com.todayyu.apis.user.notice.dto.request;

import com.todayyu.application.notice.dto.query.GetNoticesQuery;
import com.todayyu.application.notice.dto.NoticeCursor;
import com.todayyu.domain.notice.vo.NoticeId;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record GetNoticesRequest(
        Long cursorId,
        
        @Min(value = GetNoticesQuery.MIN_SIZE, message = "조회 크기는 " + GetNoticesQuery.MIN_SIZE + " 이상이어야 합니다.")
        @Max(value = GetNoticesQuery.MAX_SIZE, message = "조회 크기는 " + GetNoticesQuery.MAX_SIZE + " 이하이어야 합니다.")
        Integer size
) {
    public GetNoticesQuery toQuery() {
        NoticeCursor cursor = (cursorId == null) ? null : new NoticeCursor(new NoticeId(cursorId));
        
        return GetNoticesQuery.builder()
                .cursor(cursor)
                .size((size == null) ? GetNoticesQuery.DEFAULT_SIZE : size)
                .build();
    }
}
