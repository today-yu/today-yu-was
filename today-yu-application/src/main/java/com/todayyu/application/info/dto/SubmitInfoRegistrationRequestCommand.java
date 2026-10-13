package com.todayyu.application.info.dto;

import com.todayyu.domain.common.vo.Email;
import com.todayyu.domain.info.vo.InfoCategory;
import com.todayyu.domain.info.vo.InfoRegistrationRequestContent;
import com.todayyu.domain.member.vo.MemberId;

public record SubmitInfoRegistrationRequestCommand(
        MemberId requesterId,
        InfoCategory category,
        InfoRegistrationRequestContent content,
        Email contactEmail
) {
}
