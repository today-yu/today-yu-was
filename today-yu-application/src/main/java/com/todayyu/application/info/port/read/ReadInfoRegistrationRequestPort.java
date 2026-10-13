package com.todayyu.application.info.port.read;

import com.todayyu.application.info.port.dto.InfoRegistrationRequestView;
import com.todayyu.domain.info.vo.InfoId;
import com.todayyu.domain.info.vo.InfoRequestStatus;

import java.util.List;

public interface ReadInfoRegistrationRequestPort {
    List<InfoRegistrationRequestView> readByInfoIdAndStatus(InfoId infoId, InfoRequestStatus status);
}
