package com.todayyu.application.info.dto.result;

import com.todayyu.application.info.port.dto.InfoUpdateRequestSummaryView;
import com.todayyu.domain.info.vo.InfoCategory;
import com.todayyu.domain.info.vo.InfoRequestStatus;
import com.todayyu.domain.info.vo.InfoTitle;
import com.todayyu.domain.info.vo.InfoUpdateRequestId;

import java.time.LocalDateTime;

public record InfoUpdateRequestSummaryResult(
        InfoUpdateRequestId id,
        InfoCategory category,
        InfoTitle title,
        InfoRequestStatus status,
        LocalDateTime requestedAt
) {
    public static InfoUpdateRequestSummaryResult from(InfoUpdateRequestSummaryView view) {
        return new InfoUpdateRequestSummaryResult(
                view.id(),
                view.category(),
                view.title(),
                view.status(),
                view.requestedAt()
        );
    }
}
