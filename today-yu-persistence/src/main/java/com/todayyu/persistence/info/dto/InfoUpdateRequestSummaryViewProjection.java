package com.todayyu.persistence.info.dto;

import com.todayyu.application.info.port.dto.InfoUpdateRequestSummaryView;
import com.todayyu.domain.info.vo.InfoCategory;
import com.todayyu.domain.info.vo.InfoRequestStatus;
import com.todayyu.domain.info.vo.InfoTitle;
import com.todayyu.domain.info.vo.InfoUpdateRequestId;

import java.time.LocalDateTime;

public record InfoUpdateRequestSummaryViewProjection(
        Long id,
        InfoCategory category,
        String title,
        InfoRequestStatus status,
        LocalDateTime requestedAt
) {
    public InfoUpdateRequestSummaryView toView() {
        return InfoUpdateRequestSummaryView.builder()
                .id(new InfoUpdateRequestId(id))
                .category(category)
                .title(new InfoTitle(title))
                .status(status)
                .requestedAt(requestedAt)
                .build();
    }
}
