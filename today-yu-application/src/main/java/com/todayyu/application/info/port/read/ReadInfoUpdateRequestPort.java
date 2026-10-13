package com.todayyu.application.info.port.read;

import com.todayyu.application.info.port.dto.InfoUpdateRequestView;
import com.todayyu.domain.info.vo.InfoId;
import com.todayyu.domain.info.vo.InfoRequestStatus;

import java.util.List;

public interface ReadInfoUpdateRequestPort {
    List<InfoUpdateRequestView> readByInfoIdAndStatus(InfoId infoId, InfoRequestStatus status);
}
