package com.todayyu.application.notice.port.dto;

import com.todayyu.domain.notice.vo.NoticeId;
import com.todayyu.domain.notice.vo.NoticeTitle;

import java.time.LocalDateTime;

public record NoticeSummaryView(
        NoticeId id,
        NoticeTitle title,
        LocalDateTime publishedAt
) {
}
