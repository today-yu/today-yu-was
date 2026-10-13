package com.todayyu.application.info.dto.command;

import com.todayyu.domain.common.vo.Email;
import com.todayyu.domain.info.vo.InfoId;
import com.todayyu.domain.info.vo.InfoUpdateRequestContent;
import com.todayyu.domain.member.vo.MemberId;

import java.time.LocalDateTime;

public record SubmitInfoUpdateRequestCommand(
        MemberId requesterId,
        InfoId infoId,
        InfoUpdateRequestContent content,
        Email contactEmail,
        LocalDateTime observedAt
) {
}
