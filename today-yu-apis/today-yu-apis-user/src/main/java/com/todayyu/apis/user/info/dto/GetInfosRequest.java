package com.todayyu.apis.user.info.dto;

import com.todayyu.application.info.dto.GetInfosQuery;
import com.todayyu.application.info.dto.InfoCursor;
import com.todayyu.domain.info.vo.InfoId;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record GetInfosRequest(
        InfoCategoryParam category,
        
        @NotNull(message = "정보 정렬 타입은 비어있을 수 없습니다.")
        InfoSortTypeParam sortType,
        LocalDateTime cursorUpdatedAt,
        Long cursorInfoId,
        
        @Min(value = GetInfosQuery.MIN_SIZE, message = "조회 크기는 " + GetInfosQuery.MIN_SIZE + " 이상이어야 합니다.")
        @Max(value = GetInfosQuery.MAX_SIZE, message = "조회 크기는 " + GetInfosQuery.MAX_SIZE + " 이하이어야 합니다.")
        Integer size
) {
    public GetInfosQuery toQuery() {
        InfoCursor cursor = cursorUpdatedAt == null || cursorInfoId == null
                ? null
                : new InfoCursor(cursorUpdatedAt, new InfoId(cursorInfoId));
        
        return GetInfosQuery.builder()
                .category(category == null ? null : category.toDomain())
                .sortType(sortType.toDomain())
                .cursor(cursor)
                .size(size == null ? GetInfosQuery.DEFAULT_SIZE : size)
                .build();
    }
    
    @AssertTrue(message = "커서 값은 모두 존재하거나 모두 비어있어야 합니다.")
    private boolean isCursorValid() {
        return (cursorUpdatedAt == null) == (cursorInfoId == null);
    }
}
