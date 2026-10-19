package com.todayyu.apis.user.scrap.dto.request;

import com.todayyu.apis.user.info.dto.request.InfoCategoryParam;
import com.todayyu.application.scrap.dto.ScrapCursor;
import com.todayyu.application.scrap.dto.query.GetScrapsQuery;
import com.todayyu.domain.info.vo.InfoId;
import com.todayyu.domain.member.vo.MemberId;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record GetScrapsRequest(
        InfoCategoryParam category,
        
        @NotNull(message = "정렬 기준은 비어있을 수 없습니다.") ScrapSortTypeParam sortType,
        LocalDateTime cursorUpdatedAt, Long cursorInfoId,
        
        @Min(value = GetScrapsQuery.MIN_SIZE, message = "조회 크기는 " + GetScrapsQuery.MIN_SIZE + " 이상이어야 합니다.") @Max(value = GetScrapsQuery.MAX_SIZE, message = "조회 크기는 " + GetScrapsQuery.MAX_SIZE + " 이하이어야 합니다.") Integer size
) {
    public GetScrapsQuery toQuery(MemberId memberId) {
        ScrapCursor cursor = (cursorUpdatedAt == null || cursorInfoId == null) ? null : new ScrapCursor(cursorUpdatedAt, new InfoId(cursorInfoId));
        
        return GetScrapsQuery.builder()
                .memberId(memberId)
                .category(category == null ? null : category.toDomain())
                .cursor(cursor)
                .sortType(sortType.toDomain())
                .size(size == null ? GetScrapsQuery.DEFAULT_SIZE : size)
                .build();
    }
    
    @AssertTrue(message = "커서 값은 모두 존재하거나 비어있어야 합니다.")
    private boolean isCursorValid() {
        return (cursorUpdatedAt == null) == (cursorInfoId == null);
    }
}
