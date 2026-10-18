package com.todayyu.application.notice.dto.query;

import com.todayyu.application.notice.port.dto.NoticeCursor;
import lombok.Builder;

@Builder
public record GetNoticesQuery(
        NoticeCursor cursor,
        int size
) {
    public static final int DEFAULT_SIZE = 10;
    public static final int MAX_SIZE = 50;
    public static final int MIN_SIZE = 1;
    
    public GetNoticesQuery {
        if (size < MIN_SIZE || size > MAX_SIZE) {
            throw new IllegalArgumentException("조회 크기는 " + MIN_SIZE + " 이상 " + MAX_SIZE + " 이하여야 합니다.");
        }
    }
}
