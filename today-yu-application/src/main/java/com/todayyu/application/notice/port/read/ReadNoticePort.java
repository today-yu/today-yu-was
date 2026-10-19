package com.todayyu.application.notice.port.read;

import com.todayyu.application.notice.dto.NoticeCursor;
import com.todayyu.application.notice.port.dto.NoticeSummaryView;
import com.todayyu.domain.notice.entity.Notice;
import com.todayyu.domain.notice.vo.NoticeId;

import java.util.List;
import java.util.Optional;

public interface ReadNoticePort {
    Optional<Notice> readById(NoticeId id);
    List<NoticeSummaryView> readByCursor(NoticeCursor cursor, int limit);
}
