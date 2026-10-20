package com.todayyu.application.inquiry.dto.command;

import com.todayyu.domain.inquiry.vo.InquiryId;
import com.todayyu.domain.member.vo.MemberId;

public record DeleteInquiryCommand(
        InquiryId id,
        MemberId memberId
) {
}
