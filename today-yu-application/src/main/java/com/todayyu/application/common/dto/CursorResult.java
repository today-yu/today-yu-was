package com.todayyu.application.common.dto;

import java.util.List;

public record CursorResult<T, C>(
        List<T> contents,
        boolean hasNext,
        C nextCursor
) {
}
