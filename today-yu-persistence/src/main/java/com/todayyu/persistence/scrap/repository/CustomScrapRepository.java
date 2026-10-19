package com.todayyu.persistence.scrap.repository;

import com.todayyu.application.scrap.dto.ScrapSortType;
import com.todayyu.domain.info.vo.InfoCategory;
import com.todayyu.persistence.scrap.dto.ScrapViewProjection;

import java.time.LocalDateTime;
import java.util.List;

public interface CustomScrapRepository {
    List<Long> findInfoIdsByMemberIdAndInfoIds(Long memberId, List<Long> infoIds);
    List<ScrapViewProjection> findByCursor(
            Long memberId,
            InfoCategory category,
            ScrapSortType sortType,
            LocalDateTime cursorUpdatedAt,
            Long cursorInfoId,
            int limit
    );
}
