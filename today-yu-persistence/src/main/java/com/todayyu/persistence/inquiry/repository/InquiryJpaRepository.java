package com.todayyu.persistence.inquiry.repository;

import com.todayyu.persistence.inquiry.entity.InquiryJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InquiryJpaRepository extends JpaRepository<InquiryJpaEntity, Long> {
}
