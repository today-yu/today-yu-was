package com.todayyu.application.notice.port.dto;

import com.todayyu.domain.notice.vo.NoticeId;
import com.todayyu.domain.notice.vo.NoticeTitle;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record NoticeSummaryView(
        NoticeId id,
        NoticeTitle title,
        LocalDateTime publishedAt
) {
}
