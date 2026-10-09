package com.todayyu.application.info.dto;

import com.todayyu.domain.info.vo.InfoId;
import com.todayyu.domain.member.vo.MemberId;

public record GetInfoDetailQuery (
        InfoId infoId,
        MemberId memberId
) {
}
