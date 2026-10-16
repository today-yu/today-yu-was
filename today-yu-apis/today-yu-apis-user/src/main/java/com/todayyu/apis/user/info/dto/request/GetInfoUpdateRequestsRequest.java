package com.todayyu.apis.user.info.dto.request;

import com.todayyu.application.info.dto.query.GetInfoUpdateRequestsQuery;
import com.todayyu.application.info.port.dto.InfoUpdateRequestCursor;
import com.todayyu.domain.info.vo.InfoUpdateRequestId;
import com.todayyu.domain.member.vo.MemberId;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record GetInfoUpdateRequestsRequest(
        InfoCategoryParam category,
        
        @NotNull(message = "정렬 기준은 비어있을 수 없습니다.")
        InfoUpdateRequestSortTypeParam sortType,
        LocalDateTime cursorRequestedAt,
        Long cursorId,
        
        @Min(value = GetInfoUpdateRequestsQuery.MIN_SIZE, message = "조회 크기는 " + GetInfoUpdateRequestsQuery.MIN_SIZE + " 이상이어야 합니다.")
        @Max(value = GetInfoUpdateRequestsQuery.MAX_SIZE, message = "조회 크기는 " + GetInfoUpdateRequestsQuery.MAX_SIZE + " 이하이어야 합니다.")
        Integer size
) {
    public GetInfoUpdateRequestsQuery toQuery(MemberId requesterId) {
        InfoUpdateRequestCursor cursor = (cursorRequestedAt == null || cursorId == null)
                ? null
                : new InfoUpdateRequestCursor(cursorRequestedAt, new InfoUpdateRequestId(cursorId));
        
        return GetInfoUpdateRequestsQuery.builder()
                .requesterId(requesterId)
                .category(category == null ? null : category.toDomain())
                .sortType(sortType.toDomain())
                .cursor(cursor)
                .size(size == null ? GetInfoUpdateRequestsQuery.DEFAULT_SIZE : size)
                .build();
    }
    
    @AssertTrue(message = "커서 값은 모두 존재하거나 모두 비어있어야 합니다.")
    private boolean isCursorValid() {
        return (cursorRequestedAt == null) == (cursorId == null);
    }
}
