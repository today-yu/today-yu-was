package com.todayyu.application.info.dto;

import com.todayyu.domain.info.vo.InfoCategory;
import com.todayyu.domain.info.vo.InfoId;
import com.todayyu.domain.info.vo.InfoTitle;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record InfoResult(
        InfoId infoId,
        InfoCategory category,
        InfoTitle title,
        long helpfulMarkCount,
        LocalDateTime updatedAt
) {
    public static InfoResult of(InfoSummaryView view, long helpfulMarkCount) {
        return new InfoResult(
                view.infoId(),
                view.category(),
                view.title(),
                helpfulMarkCount,
                view.updatedAt()
        );
    }
}
