package com.todayyu.application.scrap.dto.command;

import com.todayyu.domain.info.vo.InfoId;
import com.todayyu.domain.member.vo.MemberId;

public record ScrapCommand(
        InfoId infoId,
        MemberId memberId
) {
}
