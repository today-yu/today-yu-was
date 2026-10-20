package com.todayyu.application.inquiry.dto.result;

import com.todayyu.domain.common.vo.Email;
import com.todayyu.domain.inquiry.entity.Inquiry;
import com.todayyu.domain.inquiry.vo.InquiryContent;
import com.todayyu.domain.inquiry.vo.InquiryId;
import com.todayyu.domain.inquiry.vo.InquiryStatus;
import com.todayyu.domain.inquiry.vo.InquiryTitle;
import com.todayyu.domain.member.vo.MemberId;

import java.time.LocalDateTime;

public record InquiryDetailResult(
        InquiryId id,
        MemberId memberId,
        Email contactEmail,
        InquiryStatus status,
        InquiryTitle title,
        InquiryContent content,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime answeredAt
) {
    public static InquiryDetailResult from(Inquiry inquiry) {
        return new InquiryDetailResult(
                inquiry.getId(),
                inquiry.getMemberId(),
                inquiry.getContactEmail(),
                inquiry.getStatus(),
                inquiry.getTitle(),
                inquiry.getContent(),
                inquiry.getCreatedAt(),
                inquiry.getUpdatedAt(),
                inquiry.getAnsweredAt()
        );
    }
}
