package com.todayyu.persistence.notice.dto;

import com.todayyu.application.notice.port.dto.NoticeSummaryView;
import com.todayyu.domain.notice.vo.NoticeId;
import com.todayyu.domain.notice.vo.NoticeTitle;

import java.time.LocalDateTime;

public record NoticeSummaryViewProjection(
        Long id,
        String title,
        LocalDateTime publishedAt
) {
    public NoticeSummaryView toView() {
        return NoticeSummaryView.builder()
                .id(new NoticeId(id))
                .title(new NoticeTitle(title))
                .publishedAt(publishedAt)
                .build();
    }
}
