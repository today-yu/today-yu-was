package com.todayyu.application.info.port.read;

import com.todayyu.domain.info.entity.InfoViewCount;
import com.todayyu.domain.info.vo.InfoId;

import java.util.Optional;

public interface ReadInfoViewCountPort {
    Optional<InfoViewCount> readByInfoId(InfoId infoId);
}
