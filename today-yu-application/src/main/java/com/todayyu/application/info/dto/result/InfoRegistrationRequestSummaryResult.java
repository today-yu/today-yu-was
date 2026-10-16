package com.todayyu.application.info.dto.result;

import com.todayyu.application.info.port.dto.InfoRegistrationRequestSummaryView;
import com.todayyu.domain.info.vo.InfoCategory;
import com.todayyu.domain.info.vo.InfoRegistrationRequestId;
import com.todayyu.domain.info.vo.InfoRegistrationRequestTitle;
import com.todayyu.domain.info.vo.InfoRequestStatus;

import java.time.LocalDateTime;

public record InfoRegistrationRequestSummaryResult(
        InfoRegistrationRequestId id,
        InfoCategory category,
        InfoRegistrationRequestTitle title,
        InfoRequestStatus status,
        LocalDateTime requestedAt
) {
    public static InfoRegistrationRequestSummaryResult from(InfoRegistrationRequestSummaryView view) {
        return new InfoRegistrationRequestSummaryResult(
                view.id(),
                view.category(),
                view.title(),
                view.status(),
                view.requestedAt()
        );
    }
}
