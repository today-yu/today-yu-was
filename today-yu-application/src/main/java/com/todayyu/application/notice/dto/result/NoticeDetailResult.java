package com.todayyu.application.notice.dto.result;

import com.todayyu.domain.notice.entity.Notice;
import com.todayyu.domain.notice.vo.NoticeContent;
import com.todayyu.domain.notice.vo.NoticeId;
import com.todayyu.domain.notice.vo.NoticeTitle;

import java.time.LocalDateTime;

public record NoticeDetailResult(
        NoticeId id,
        NoticeTitle title,
        NoticeContent content,
        LocalDateTime publishedAt,
        LocalDateTime updatedAt
) {
    public static NoticeDetailResult from(Notice notice) {
        return new NoticeDetailResult(
                notice.getId(),
                notice.getTitle(),
                notice.getContent(),
                notice.getPublishedAt(),
                notice.getUpdatedAt()
        );
    }
}
