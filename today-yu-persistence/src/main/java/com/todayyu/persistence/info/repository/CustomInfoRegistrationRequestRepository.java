package com.todayyu.persistence.info.repository;

import com.todayyu.application.info.dto.InfoRegistrationRequestSortType;
import com.todayyu.domain.info.vo.InfoCategory;
import com.todayyu.domain.info.vo.InfoRequestStatus;
import com.todayyu.persistence.info.dto.InfoRegistrationRequestProjection;
import com.todayyu.persistence.info.dto.InfoRegistrationRequestSummaryViewProjection;

import java.time.LocalDateTime;
import java.util.List;

public interface CustomInfoRegistrationRequestRepository {
    List<InfoRegistrationRequestProjection> findWithRequesterNicknameByInfoIdAndStatus(
            Long infoId,
            InfoRequestStatus status
    );
    List<InfoRegistrationRequestSummaryViewProjection> findByCursor(
            Long requesterId,
            InfoCategory category,
            InfoRegistrationRequestSortType sortType,
            LocalDateTime cursorRequestedAt,
            Long cursorId,
            int limit
    );
}
