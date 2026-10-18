package com.todayyu.application.notice.dto.result;

import com.todayyu.application.notice.port.dto.NoticeSummaryView;
import com.todayyu.domain.notice.vo.NoticeId;
import com.todayyu.domain.notice.vo.NoticeTitle;

import java.time.LocalDateTime;

public record NoticeSummaryResult(
        NoticeId id,
        NoticeTitle title,
        LocalDateTime publishedAt
) {
    public static NoticeSummaryResult from(NoticeSummaryView view) {
        return new NoticeSummaryResult(
                view.id(),
                view.title(),
                view.publishedAt()
        );
    }
}
