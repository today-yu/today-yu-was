package com.todayyu.application.inquiry.port.save;

import com.todayyu.domain.inquiry.entity.Inquiry;
import com.todayyu.domain.inquiry.vo.InquiryId;

public interface SaveInquiryPort {
    Inquiry save(Inquiry inquiry);
    void deleteById(InquiryId id);
}
