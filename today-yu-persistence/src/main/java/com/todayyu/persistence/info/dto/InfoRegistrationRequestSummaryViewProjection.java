package com.todayyu.persistence.info.dto;

import com.todayyu.application.info.port.dto.InfoRegistrationRequestSummaryView;
import com.todayyu.domain.info.vo.InfoCategory;
import com.todayyu.domain.info.vo.InfoRegistrationRequestId;
import com.todayyu.domain.info.vo.InfoRegistrationRequestTitle;
import com.todayyu.domain.info.vo.InfoRequestStatus;

import java.time.LocalDateTime;

public record InfoRegistrationRequestSummaryViewProjection(
        Long id,
        InfoCategory category,
        String title,
        LocalDateTime requestedAt,
        InfoRequestStatus status
) {
    public InfoRegistrationRequestSummaryView toView() {
        return InfoRegistrationRequestSummaryView.builder()
                .id(new InfoRegistrationRequestId(id))
                .category(category)
                .title(new InfoRegistrationRequestTitle(title))
                .status(status)
                .requestedAt(requestedAt)
                .build();
    }
}
