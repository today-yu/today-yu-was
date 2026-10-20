package com.todayyu.application.inquiry.port.read;

import com.todayyu.domain.inquiry.entity.Inquiry;
import com.todayyu.domain.inquiry.vo.InquiryId;

import java.util.Optional;

public interface ReadInquiryPort {
    Optional<Inquiry> readById(InquiryId id);
}
