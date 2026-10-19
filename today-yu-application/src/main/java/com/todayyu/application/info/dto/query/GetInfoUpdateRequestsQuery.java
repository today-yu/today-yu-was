package com.todayyu.application.info.dto.query;

import com.todayyu.application.info.dto.InfoUpdateRequestCursor;
import com.todayyu.application.info.port.dto.InfoUpdateRequestSortType;
import com.todayyu.domain.info.vo.InfoCategory;
import com.todayyu.domain.member.vo.MemberId;
import lombok.Builder;

@Builder
public record GetInfoUpdateRequestsQuery(
        MemberId requesterId,
        InfoCategory category,
        InfoUpdateRequestSortType sortType,
        InfoUpdateRequestCursor cursor,
        int size
) {
    public static final int DEFAULT_SIZE = 10;
    public static final int MAX_SIZE = 50;
    public static final int MIN_SIZE = 1;
    
    public GetInfoUpdateRequestsQuery {
        if (requesterId == null) {
            throw new IllegalArgumentException("정보 수정 요청자 ID는 비어있을 수 없습니다.");
        }
        
        if (sortType == null) {
            throw new IllegalArgumentException("정렬 기준은 비어있을 수 없습니다.");
        }
        
        if (size < MIN_SIZE || size > MAX_SIZE) {
            throw new IllegalArgumentException("조회 크기는 " + MIN_SIZE + " 이상 " + MAX_SIZE + " 이하여야 합니다.");
        }
    }
}
