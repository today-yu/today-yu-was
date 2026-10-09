package com.todayyu.application.info.port.read;

import com.todayyu.domain.info.entity.Info;
import com.todayyu.domain.info.vo.InfoId;

import java.util.Optional;

public interface ReadInfoPort {
    Optional<Info> readById(InfoId id);
}
