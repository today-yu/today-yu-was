package com.todayyu.application.inquiry.port.save;

import com.todayyu.domain.inquiry.entity.Inquiry;

public interface SaveInquiryPort {
    Inquiry save(Inquiry inquiry);
}
