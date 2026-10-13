package com.todayyu.application.info.port.save;

import com.todayyu.domain.info.entity.InfoUpdateRequest;

public interface SaveInfoUpdateRequestPort {
    InfoUpdateRequest save(InfoUpdateRequest infoUpdateRequest);
}
