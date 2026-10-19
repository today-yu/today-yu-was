package com.todayyu.persistence.inquiry.mapper;

import com.todayyu.domain.common.vo.Email;
import com.todayyu.domain.inquiry.entity.Inquiry;
import com.todayyu.domain.inquiry.vo.InquiryContent;
import com.todayyu.domain.inquiry.vo.InquiryId;
import com.todayyu.domain.inquiry.vo.InquiryTitle;
import com.todayyu.domain.member.vo.MemberId;
import com.todayyu.persistence.common.Mapper;
import com.todayyu.persistence.inquiry.entity.InquiryJpaEntity;

@Mapper
public class InquiryMapper {
    /**
     * {@link Inquiry}를 {@link InquiryJpaEntity}로 변환하는 메서드
     *
     * @param domainEntity {@link Inquiry} 객체
     * @return {@link InquiryJpaEntity} 객체
     */
    public InquiryJpaEntity toJpaEntity(Inquiry domainEntity) {
        return InquiryJpaEntity.builder()
                .memberId(domainEntity.getMemberId().getValue())
                .contactEmail(domainEntity.getContactEmail().getValue())
                .status(domainEntity.getStatus())
                .title(domainEntity.getTitle().getValue())
                .content(domainEntity.getContent().getValue())
                .createdAt(domainEntity.getCreatedAt())
                .updatedAt(domainEntity.getUpdatedAt())
                .answeredAt(domainEntity.getAnsweredAt())
                .build();
    }

    /**
     * {@link InquiryJpaEntity}를 {@link Inquiry}로 변환하는 메서드
     *
     * @param jpaEntity {@link InquiryJpaEntity} 객체
     * @return {@link Inquiry} 객체
     */
    public Inquiry toDomain(InquiryJpaEntity jpaEntity) {
        return Inquiry.withId(
                new InquiryId(jpaEntity.getId()),
                new MemberId(jpaEntity.getMemberId()),
                new Email(jpaEntity.getContactEmail()),
                jpaEntity.getStatus(),
                new InquiryTitle(jpaEntity.getTitle()),
                new InquiryContent(jpaEntity.getContent()),
                jpaEntity.getCreatedAt(),
                jpaEntity.getUpdatedAt(),
                jpaEntity.getAnsweredAt()
        );
    }
}
