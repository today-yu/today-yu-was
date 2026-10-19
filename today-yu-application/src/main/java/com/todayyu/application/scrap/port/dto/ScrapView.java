package com.todayyu.application.scrap.port.dto;

import com.todayyu.domain.info.vo.InfoCategory;
import com.todayyu.domain.info.vo.InfoId;
import com.todayyu.domain.info.vo.InfoTitle;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record ScrapView(
        InfoId infoId,
        InfoCategory category,
        InfoTitle title,
        long helpfulMarkCount,
        LocalDateTime updatedAt
) {
}
