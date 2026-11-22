package com.todayyu.application.info.port.save;

import com.todayyu.domain.info.entity.InfoUpdateRequest;
import com.todayyu.domain.info.vo.InfoUpdateRequestId;

import java.util.List;

public interface SaveInfoUpdateRequestPort {
    InfoUpdateRequest save(InfoUpdateRequest infoUpdateRequest);
    void deleteByIds(List<InfoUpdateRequestId> ids);
    void anonymizeAll(List<InfoUpdateRequest> requests);
}
