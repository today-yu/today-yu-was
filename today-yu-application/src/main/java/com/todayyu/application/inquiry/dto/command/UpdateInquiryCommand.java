package com.todayyu.application.inquiry.dto.command;

import com.todayyu.domain.common.vo.Email;
import com.todayyu.domain.inquiry.vo.InquiryContent;
import com.todayyu.domain.inquiry.vo.InquiryId;
import com.todayyu.domain.inquiry.vo.InquiryTitle;
import com.todayyu.domain.member.vo.MemberId;
import lombok.Builder;

@Builder
public record UpdateInquiryCommand(
        InquiryId id,
        InquiryTitle title,
        InquiryContent content,
        Email contactEmail,
        MemberId memberId
) {
}
