package com.todayyu.application.info.dto.result;

import com.todayyu.application.info.port.dto.InfoSummaryView;
import com.todayyu.domain.info.vo.InfoCategory;
import com.todayyu.domain.info.vo.InfoId;
import com.todayyu.domain.info.vo.InfoTitle;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record InfoSummaryResult(
        InfoId infoId,
        InfoCategory category,
        InfoTitle title,
        long helpfulMarkCount,
        LocalDateTime updatedAt,
        boolean scrapped
) {
    public static InfoSummaryResult of(InfoSummaryView view, long helpfulMarkCount, boolean scrapped) {
        return new InfoSummaryResult(
                view.infoId(),
                view.category(),
                view.title(),
                helpfulMarkCount,
                view.updatedAt(),
                scrapped
        );
    }
}
