package com.todayyu.application.inquiry.dto.command;

import com.todayyu.domain.common.vo.Email;
import com.todayyu.domain.inquiry.vo.InquiryContent;
import com.todayyu.domain.inquiry.vo.InquiryTitle;
import com.todayyu.domain.member.vo.MemberId;

public record SubmitInquiryCommand(
        MemberId memberId,
        Email contactEmail,
        InquiryTitle title,
        InquiryContent content
) {
}
