package com.todayyu.persistence.info.repository;

import com.todayyu.application.info.port.dto.InfoUpdateRequestSortType;
import com.todayyu.domain.info.vo.InfoCategory;
import com.todayyu.domain.info.vo.InfoRequestStatus;
import com.todayyu.persistence.info.dto.InfoUpdateRequestProjection;
import com.todayyu.persistence.info.dto.InfoUpdateRequestSummaryViewProjection;

import java.time.LocalDateTime;
import java.util.List;

public interface CustomInfoUpdateRequestRepository {
    List<InfoUpdateRequestProjection> findWithRequesterNicknameByInfoIdAndStatus(Long infoId, InfoRequestStatus status);
    List<InfoUpdateRequestSummaryViewProjection> findByCursor(
            Long requesterId,
            InfoCategory category,
            InfoUpdateRequestSortType sortType,
            LocalDateTime cursorRequestedAt,
            Long cursorId,
            int limit
    );
}
