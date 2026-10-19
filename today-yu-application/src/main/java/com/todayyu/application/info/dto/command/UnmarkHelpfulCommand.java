package com.todayyu.application.info.dto.command;

import com.todayyu.domain.info.vo.InfoId;
import com.todayyu.domain.member.vo.MemberId;

public record UnmarkHelpfulCommand(
        InfoId infoId,
        MemberId memberId
) {
}
