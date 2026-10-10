package com.todayyu.application.info.dto;

import com.todayyu.domain.info.vo.InfoCategory;

public record GetInfosQuery(
        InfoCategory category,
        InfoSortType sortType,
        InfoCursor cursor,
        int size
) {
    public static final int DEFAULT_SIZE = 10;
    public static final int MAX_SIZE = 50;
    
    public GetInfosQuery {
        if (sortType == null) {
            throw new IllegalArgumentException("정렬 기준은 비어있을 수 없습니다.");
        }
        
        if (size < 1 || size > MAX_SIZE) {
            throw new IllegalArgumentException("조회 크기는 1 이상 " + MAX_SIZE + " 이하여야 합니다.");
        }
    }
}
