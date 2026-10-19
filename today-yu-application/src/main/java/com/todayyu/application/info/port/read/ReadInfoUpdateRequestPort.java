package com.todayyu.application.info.port.read;

import com.todayyu.application.info.dto.InfoUpdateRequestCursor;
import com.todayyu.application.info.dto.InfoUpdateRequestSortType;
import com.todayyu.application.info.port.dto.InfoUpdateRequestSummaryView;
import com.todayyu.application.info.port.dto.InfoUpdateRequestView;
import com.todayyu.domain.info.entity.InfoUpdateRequest;
import com.todayyu.domain.info.vo.InfoCategory;
import com.todayyu.domain.info.vo.InfoId;
import com.todayyu.domain.info.vo.InfoRequestStatus;
import com.todayyu.domain.info.vo.InfoUpdateRequestId;
import com.todayyu.domain.member.vo.MemberId;

import java.util.List;
import java.util.Optional;

public interface ReadInfoUpdateRequestPort {
    Optional<InfoUpdateRequest> readById(InfoUpdateRequestId id);
    List<InfoUpdateRequestView> readByInfoIdAndStatus(InfoId infoId, InfoRequestStatus status);
    List<InfoUpdateRequestSummaryView> readByCursor(
            MemberId requesterId,
            InfoCategory category,
            InfoUpdateRequestSortType sortType,
            InfoUpdateRequestCursor cursor,
            int limit
    );
}
