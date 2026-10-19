package com.todayyu.application.scrap.dto;

import com.todayyu.domain.info.vo.InfoId;

import java.time.LocalDateTime;

public record ScrapCursor(
        LocalDateTime updatedAt,
        InfoId infoId
) {
    public ScrapCursor {
        if (updatedAt == null || infoId == null) {
            throw new IllegalArgumentException("커서 값은 비어있을 수 없습니다.");
        }
    }
}
