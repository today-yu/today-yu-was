package com.todayyu.application.info.port.save;

import com.todayyu.domain.info.entity.InfoRegistrationRequest;
import com.todayyu.domain.info.vo.InfoRegistrationRequestId;

import java.util.List;

public interface SaveInfoRegistrationRequestPort {
    InfoRegistrationRequest save(InfoRegistrationRequest infoRegistrationRequest);
    void deleteByIds(List<InfoRegistrationRequestId> ids);
    void anonymizeAll(List<InfoRegistrationRequest> requests);
}
