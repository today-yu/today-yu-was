package com.todayyu.persistence.scrap.dto;

import com.todayyu.application.scrap.port.dto.ScrapView;
import com.todayyu.domain.info.vo.InfoCategory;
import com.todayyu.domain.info.vo.InfoId;
import com.todayyu.domain.info.vo.InfoTitle;

import java.time.LocalDateTime;

public record ScrapViewProjection(
        Long infoId,
        InfoCategory category,
        String title,
        Long helpfulMarkCount,
        LocalDateTime updatedAt
) {
    public ScrapView toView() {
        return ScrapView.builder()
                .infoId(new InfoId(infoId))
                .category(category)
                .title(new InfoTitle(title))
                .helpfulMarkCount(helpfulMarkCount)
                .updatedAt(updatedAt)
                .build();
    }
}
