package com.todayyu.application.info.port.dto;

import com.todayyu.domain.info.vo.InfoCategory;
import com.todayyu.domain.info.vo.InfoRegistrationRequestId;
import com.todayyu.domain.info.vo.InfoRegistrationRequestTitle;
import com.todayyu.domain.info.vo.InfoRequestStatus;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record InfoRegistrationRequestSummaryView(
        InfoRegistrationRequestId id,
        InfoCategory category,
        InfoRegistrationRequestTitle title,
        InfoRequestStatus status,
        LocalDateTime requestedAt
) {
}
