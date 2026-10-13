package com.todayyu.persistence.info.dto;

import com.todayyu.application.info.port.dto.InfoSummaryView;
import com.todayyu.domain.info.vo.InfoCategory;
import com.todayyu.domain.info.vo.InfoId;
import com.todayyu.domain.info.vo.InfoTitle;

import java.time.LocalDateTime;

public record InfoSummaryViewProjection(
        Long infoId,
        InfoCategory category,
        String title,
        LocalDateTime updatedAt
) {
    public InfoSummaryView toView() {
        return InfoSummaryView.builder()
                .infoId(new InfoId(infoId))
                .category(category)
                .title(new InfoTitle(title))
                .updatedAt(updatedAt)
                .build();
    }
}
