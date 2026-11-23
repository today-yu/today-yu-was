package com.todayyu.persistence.inquiry.repository;

import com.todayyu.persistence.inquiry.entity.InquiryJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InquiryJpaRepository extends JpaRepository<InquiryJpaEntity, Long>, CustomInquiryRepository {
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM InquiryJpaEntity i WHERE i.memberId = :memberId")
    void deleteAllByMemberIdInBatch(@Param(value = "memberId") Long memberId);
}
