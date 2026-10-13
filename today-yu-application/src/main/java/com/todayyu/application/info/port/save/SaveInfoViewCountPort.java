package com.todayyu.application.info.port.save;

import com.todayyu.domain.info.vo.InfoId;

public interface SaveInfoViewCountPort {
    void increase(InfoId infoId);
}
