package com.todayyu.persistence.info.repository;

import com.todayyu.domain.info.vo.InfoRequestStatus;
import com.todayyu.persistence.info.dto.InfoUpdateRequestProjection;

import java.util.List;

public interface CustomInfoUpdateRequestRepository {
    List<InfoUpdateRequestProjection> findWithRequesterNicknameByInfoIdAndStatus(Long infoId, InfoRequestStatus status);
}
