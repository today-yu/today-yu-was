package com.todayyu.application.info.dto;

import com.todayyu.domain.info.vo.InfoCategory;
import com.todayyu.domain.info.vo.InfoId;
import com.todayyu.domain.info.vo.InfoTitle;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record InfoSummaryView(
        InfoId infoId,
        InfoCategory category,
        InfoTitle title,
        LocalDateTime updatedAt
) {
}
