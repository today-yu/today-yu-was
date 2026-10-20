package com.todayyu.persistence.inquiry.repository;

import com.todayyu.persistence.inquiry.dto.InquirySummaryViewProjection;

import java.util.List;

public interface CustomInquiryRepository {
    List<InquirySummaryViewProjection> findByCursor(Long memberId, Long cursorId, int limit);
}
