package com.todayyu.application.info.port.read;

import com.todayyu.application.info.dto.InfoRegistrationRequestCursor;
import com.todayyu.application.info.port.dto.InfoRegistrationRequestSortType;
import com.todayyu.application.info.port.dto.InfoRegistrationRequestSummaryView;
import com.todayyu.application.info.port.dto.InfoRegistrationRequestView;
import com.todayyu.domain.info.entity.InfoRegistrationRequest;
import com.todayyu.domain.info.vo.InfoCategory;
import com.todayyu.domain.info.vo.InfoId;
import com.todayyu.domain.info.vo.InfoRegistrationRequestId;
import com.todayyu.domain.info.vo.InfoRequestStatus;
import com.todayyu.domain.member.vo.MemberId;

import java.util.List;
import java.util.Optional;

public interface ReadInfoRegistrationRequestPort {
    Optional<InfoRegistrationRequest> readById(InfoRegistrationRequestId id);
    List<InfoRegistrationRequestView> readByInfoIdAndStatus(InfoId infoId, InfoRequestStatus status);
    List<InfoRegistrationRequestSummaryView> readByCursor(
            MemberId requesterId,
            InfoCategory category,
            InfoRegistrationRequestSortType sortType,
            InfoRegistrationRequestCursor cursor,
            int limit
    );
}
