package com.todayyu.application.info.port.read;

import com.todayyu.application.info.port.dto.InfoCursor;
import com.todayyu.application.info.port.dto.InfoSortType;
import com.todayyu.application.info.port.dto.InfoSummaryView;
import com.todayyu.domain.info.entity.Info;
import com.todayyu.domain.info.vo.InfoCategory;
import com.todayyu.domain.info.vo.InfoId;

import java.util.List;
import java.util.Optional;

public interface ReadInfoPort {
    Optional<Info> readById(InfoId id);
    List<InfoSummaryView> readByCursor(InfoCategory category, InfoSortType sortType, InfoCursor cursor, int limit);
    boolean existsById(InfoId id);
}
