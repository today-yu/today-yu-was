package com.todayyu.application.notice.dto;

import com.todayyu.application.notice.port.dto.NoticeSummaryView;
import com.todayyu.domain.notice.vo.NoticeId;

public record NoticeCursor(
        NoticeId noticeId
) {
    public NoticeCursor {
        if (noticeId == null) {
            throw new IllegalArgumentException("커서 값은 비어있을 수 없습니다.");
        }
    }
    
    public static NoticeCursor from(NoticeSummaryView view) {
        return new NoticeCursor(view.id());
    }
}
