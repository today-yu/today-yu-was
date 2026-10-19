package com.todayyu.apis.user.info.dto.request;

import com.todayyu.application.info.dto.query.GetInfoRegistrationRequestsQuery;
import com.todayyu.application.info.dto.InfoRegistrationRequestCursor;
import com.todayyu.domain.info.vo.InfoRegistrationRequestId;
import com.todayyu.domain.member.vo.MemberId;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record GetInfoRegistrationRequestsRequest(
        InfoCategoryParam category,
        
        @NotNull(message = "정렬 기준은 비어있을 수 없습니다.")
        InfoRegistrationRequestSortTypeParam sortType,
        LocalDateTime cursorRequestedAt,
        Long cursorId,
        
        @Min(value = GetInfoRegistrationRequestsQuery.MIN_SIZE, message = "조회 크기는 " + GetInfoRegistrationRequestsQuery.MIN_SIZE + " 이상이어야 합니다.")
        @Max(value = GetInfoRegistrationRequestsQuery.MAX_SIZE, message = "조회 크기는 " + GetInfoRegistrationRequestsQuery.MAX_SIZE + " 이하이어야 합니다.")
        Integer size
) {
    public GetInfoRegistrationRequestsQuery toQuery(MemberId requesterId) {
        InfoRegistrationRequestCursor cursor = (cursorRequestedAt == null || cursorId == null)
                ? null
                : new InfoRegistrationRequestCursor(cursorRequestedAt, new InfoRegistrationRequestId(cursorId));
        
        return GetInfoRegistrationRequestsQuery.builder()
                .requesterId(requesterId)
                .category(category == null ? null : category.toDomain())
                .sortType(sortType.toDomain())
                .cursor(cursor)
                .size(size == null ? GetInfoRegistrationRequestsQuery.DEFAULT_SIZE : size)
                .build();
    }
    
    @AssertTrue(message = "커서 값은 모두 존재하거나 모두 비어있어야 합니다.")
    private boolean isCursorValid() {
        return (cursorRequestedAt == null) == (cursorId == null);
    }
}
