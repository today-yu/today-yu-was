package com.todayyu.application.inquiry.dto.query;

import com.todayyu.domain.inquiry.vo.InquiryId;
import com.todayyu.domain.member.vo.MemberId;

public record GetInquiryDetailQuery(
        MemberId memberId,
        InquiryId inquiryId
) {
}
