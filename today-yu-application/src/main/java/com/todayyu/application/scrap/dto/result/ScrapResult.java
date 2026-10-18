package com.todayyu.application.scrap.dto.result;

import com.todayyu.application.scrap.port.dto.ScrapView;
import com.todayyu.domain.info.vo.InfoCategory;
import com.todayyu.domain.info.vo.InfoId;
import com.todayyu.domain.info.vo.InfoTitle;

import java.time.LocalDateTime;

public record ScrapResult(
        InfoId infoId,
        InfoCategory category,
        InfoTitle title,
        long helpfulMarkCount,
        LocalDateTime updatedAt
) {
    public static ScrapResult from(ScrapView view) {
        return new ScrapResult(
                view.infoId(),
                view.category(),
                view.title(),
                view.helpfulMarkCount(),
                view.updatedAt()
        );
    }
}
