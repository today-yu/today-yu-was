package com.todayyu.application.info.dto;

import com.todayyu.domain.info.vo.InfoId;

import java.time.LocalDateTime;

public record InfoCursor(
        LocalDateTime updatedAt,
        InfoId infoId
) {
    public InfoCursor {
        if (updatedAt == null || infoId == null) {
            throw new IllegalArgumentException("커서 값은 비어있을 수 없습니다.");
        }
    }
}
