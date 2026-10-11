package com.todayyu.persistence.info.repository;

import com.todayyu.application.info.dto.InfoSortType;
import com.todayyu.domain.info.vo.InfoCategory;
import com.todayyu.persistence.info.dto.InfoSummaryViewProjection;

import java.time.LocalDateTime;
import java.util.List;

public interface CustomInfoRepository {
    List<InfoSummaryViewProjection> findByCursor(
            InfoCategory category,
            InfoSortType sortType,
            LocalDateTime cursorUpdatedAt,
            Long cursorId,
            int limit
    );
}
