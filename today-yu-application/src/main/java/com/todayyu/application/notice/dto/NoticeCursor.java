package com.todayyu.application.notice.dto;

import com.todayyu.domain.notice.vo.NoticeId;

public record NoticeCursor(
        NoticeId noticeId
) {
    public NoticeCursor {
        if (noticeId == null) {
            throw new IllegalArgumentException("커서 값은 비어있을 수 없습니다.");
        }
    }
}
