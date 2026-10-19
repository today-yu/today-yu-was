package com.todayyu.application.info.port.save;

import com.todayyu.domain.info.entity.HelpfulMark;
import com.todayyu.domain.info.vo.InfoId;
import com.todayyu.domain.member.vo.MemberId;

public interface SaveHelpfulMarkPort {
    HelpfulMark save(HelpfulMark helpfulMark);
    void deleteByInfoIdAndMemberId(InfoId infoId, MemberId memberId);
}
