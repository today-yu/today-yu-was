package com.todayyu.apis.user.inquiry.dto.request;

import com.todayyu.application.inquiry.dto.InquiryCursor;
import com.todayyu.application.inquiry.dto.query.GetInquiriesQuery;
import com.todayyu.domain.inquiry.vo.InquiryId;
import com.todayyu.domain.member.vo.MemberId;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record GetInquiriesRequest(
        Long cursorId,
        
        @Min(value = GetInquiriesQuery.MIN_SIZE, message = "조회 크기는 " + GetInquiriesQuery.MIN_SIZE + " 이상이어야 합니다.")
        @Max(value = GetInquiriesQuery.MAX_SIZE, message = "조회 크기는 " + GetInquiriesQuery.MAX_SIZE + " 이하이어야 합니다.")
        Integer size
) {
    public GetInquiriesQuery toQuery(MemberId memberId) {
        InquiryCursor cursor = (cursorId == null) ? null : new InquiryCursor(new InquiryId(cursorId));
        
        return GetInquiriesQuery.builder()
                .memberId(memberId)
                .cursor(cursor)
                .size(size == null ? GetInquiriesQuery.DEFAULT_SIZE : size)
                .build();
    }
}
