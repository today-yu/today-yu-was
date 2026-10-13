package com.todayyu.application.info.port.save;

import com.todayyu.domain.info.entity.InfoRegistrationRequest;

public interface SaveInfoRegistrationRequestPort {
    InfoRegistrationRequest save(InfoRegistrationRequest infoRegistrationRequest);
}
