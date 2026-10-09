package com.todayyu.persistence.info.repository;

import com.todayyu.domain.info.vo.InfoRequestStatus;
import com.todayyu.persistence.info.dto.InfoRegistrationRequestProjection;

import java.util.List;

public interface CustomInfoRegistrationRequestRepository {
    List<InfoRegistrationRequestProjection> findWithRequesterNicknameByInfoIdAndStatus(Long infoId, InfoRequestStatus status);
}
