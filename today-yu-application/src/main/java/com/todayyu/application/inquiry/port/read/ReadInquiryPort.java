package com.todayyu.application.inquiry.port.read;

import com.todayyu.application.inquiry.dto.InquiryCursor;
import com.todayyu.application.inquiry.port.dto.InquirySummaryView;
import com.todayyu.domain.inquiry.entity.Inquiry;
import com.todayyu.domain.inquiry.vo.InquiryId;
import com.todayyu.domain.member.vo.MemberId;

import java.util.List;
import java.util.Optional;

public interface ReadInquiryPort {
    Optional<Inquiry> readById(InquiryId id);
    List<InquirySummaryView> readByCursor(MemberId memberId, InquiryCursor cursor, int limit);
}
