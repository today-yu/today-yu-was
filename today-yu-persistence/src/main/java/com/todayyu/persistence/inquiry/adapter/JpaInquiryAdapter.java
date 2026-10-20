package com.todayyu.persistence.inquiry.adapter;

import com.todayyu.application.inquiry.port.save.SaveInquiryPort;
import com.todayyu.domain.inquiry.entity.Inquiry;
import com.todayyu.domain.inquiry.vo.InquiryId;
import com.todayyu.persistence.common.PersistenceAdapter;
import com.todayyu.persistence.inquiry.entity.InquiryJpaEntity;
import com.todayyu.persistence.inquiry.mapper.InquiryMapper;
import com.todayyu.persistence.inquiry.repository.InquiryJpaRepository;
import lombok.RequiredArgsConstructor;

@PersistenceAdapter
@RequiredArgsConstructor
public class JpaInquiryAdapter implements SaveInquiryPort {
    private final InquiryJpaRepository inquiryRepository;
    private final InquiryMapper inquiryMapper;

    /**
     * 문의를 저장하는 메서드
     *
     * @param inquiry 문의 객체
     * @return 저장 후 {@link InquiryId}가 부여된 문의 객체
     */
    @Override
    public Inquiry save(Inquiry inquiry) {
        InquiryJpaEntity savedJpaEntity = inquiryRepository.save(inquiryMapper.toJpaEntity(inquiry));
        
        return inquiryMapper.toDomain(savedJpaEntity);
    }
}
