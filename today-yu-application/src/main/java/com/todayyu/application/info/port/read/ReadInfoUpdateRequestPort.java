package com.todayyu.application.info.port.read;

import com.todayyu.application.info.port.dto.InfoUpdateRequestView;
import com.todayyu.domain.info.entity.InfoUpdateRequest;
import com.todayyu.domain.info.vo.InfoId;
import com.todayyu.domain.info.vo.InfoRequestStatus;
import com.todayyu.domain.info.vo.InfoUpdateRequestId;

import java.util.List;
import java.util.Optional;

public interface ReadInfoUpdateRequestPort {
    Optional<InfoUpdateRequest> readById(InfoUpdateRequestId id);
    List<InfoUpdateRequestView> readByInfoIdAndStatus(InfoId infoId, InfoRequestStatus status);
}
