package com.todayyu.application.scrap.port.save;

import com.todayyu.domain.info.vo.InfoId;
import com.todayyu.domain.member.vo.MemberId;
import com.todayyu.domain.scrap.entity.Scrap;

public interface SaveScrapPort {
    Scrap save(Scrap scrap);
    void deleteByInfoIdAndMemberId(InfoId infoId, MemberId memberId);
}
