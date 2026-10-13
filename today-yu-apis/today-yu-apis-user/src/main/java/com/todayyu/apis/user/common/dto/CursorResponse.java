package com.todayyu.apis.user.common.dto;

import com.todayyu.application.common.dto.CursorResult;

import java.util.List;
import java.util.function.Function;

public record CursorResponse<T, C>(
        List<T> contents,
        boolean hasNext,
        C nextCursor
) {
    public static <R, RC, T, C> CursorResponse<T, C> of(
            CursorResult<R, RC> result,
            Function<R, T> contentMapper,
            Function<RC, C> cursorMapper
    ) {
        C nextCursor = result.nextCursor() == null
                ? null
                : cursorMapper.apply(result.nextCursor());

        return new CursorResponse<>(
                result.contents().stream().map(contentMapper).toList(),
                result.hasNext(),
                nextCursor
        );
    }
}
