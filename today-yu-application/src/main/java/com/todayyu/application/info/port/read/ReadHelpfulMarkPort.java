package com.todayyu.application.info.port.read;

import com.todayyu.domain.info.vo.InfoId;
import com.todayyu.domain.member.vo.MemberId;

public interface ReadHelpfulMarkPort {
    long countByInfoId(InfoId infoId);
    boolean existsByInfoIdAndMemberId(InfoId infoId, MemberId memberId);
}
