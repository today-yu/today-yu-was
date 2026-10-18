package com.todayyu.application.scrap.port.read;

import com.todayyu.domain.info.vo.InfoId;
import com.todayyu.domain.member.vo.MemberId;

import java.util.List;
import java.util.Set;

public interface ReadScrapPort {
    boolean existsByInfoIdAndMemberId(InfoId infoId, MemberId memberId);
    Set<InfoId> readScrappedInfoIds(MemberId memberId, List<InfoId> infoIds);
}
