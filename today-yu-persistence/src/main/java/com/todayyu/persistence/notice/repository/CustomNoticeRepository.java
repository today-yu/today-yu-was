package com.todayyu.persistence.notice.repository;

import com.todayyu.persistence.notice.dto.NoticeSummaryViewProjection;

import java.util.List;

public interface CustomNoticeRepository {
    List<NoticeSummaryViewProjection> findByCursor(Long cursorId, int limit);
}
