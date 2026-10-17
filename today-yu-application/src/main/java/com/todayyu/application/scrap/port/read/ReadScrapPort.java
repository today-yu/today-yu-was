package com.todayyu.application.scrap.port.read;

import com.todayyu.domain.info.vo.InfoId;
import com.todayyu.domain.member.vo.MemberId;

public interface ReadScrapPort {
    boolean existsByInfoIdAndMemberId(InfoId infoId, MemberId memberId);
}
