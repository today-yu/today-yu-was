package com.todayyu.application.info.dto;

import com.todayyu.application.info.port.dto.InfoUpdateRequestSummaryView;
import com.todayyu.domain.info.vo.InfoUpdateRequestId;

import java.time.LocalDateTime;

public record InfoUpdateRequestCursor(
        LocalDateTime requestedAt,
        InfoUpdateRequestId id
) {
    public InfoUpdateRequestCursor {
        if (requestedAt == null || id == null) {
            throw new IllegalArgumentException("커서 값은 비어있을 수 없습니다.");
        }
    }
    
    public static InfoUpdateRequestCursor from(InfoUpdateRequestSummaryView view) {
        return new InfoUpdateRequestCursor(view.requestedAt(), view.id());
    }
}
