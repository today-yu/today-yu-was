package com.todayyu.application.info.port.dto;

import com.todayyu.domain.info.vo.InfoCategory;
import com.todayyu.domain.info.vo.InfoRequestStatus;
import com.todayyu.domain.info.vo.InfoTitle;
import com.todayyu.domain.info.vo.InfoUpdateRequestId;

import java.time.LocalDateTime;

public record InfoUpdateRequestSummaryView(
        InfoUpdateRequestId id,
        InfoCategory category,
        InfoTitle title,
        InfoRequestStatus status,
        LocalDateTime requestedAt
) {
}
