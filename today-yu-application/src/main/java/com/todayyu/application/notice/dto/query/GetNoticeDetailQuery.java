package com.todayyu.application.notice.dto.query;

import com.todayyu.domain.notice.vo.NoticeId;

public record GetNoticeDetailQuery(
        NoticeId id
) {
}
