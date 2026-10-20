package com.todayyu.persistence.inquiry.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.todayyu.persistence.inquiry.dto.InquirySummaryViewProjection;
import com.todayyu.persistence.inquiry.entity.QInquiryJpaEntity;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class CustomInquiryRepositoryImpl implements CustomInquiryRepository {
    private final JPAQueryFactory jpaQueryFactory;
    
    private final QInquiryJpaEntity inquiry = QInquiryJpaEntity.inquiryJpaEntity;
    
    @Override
    public List<InquirySummaryViewProjection> findByCursor(Long memberId, Long cursorId, int limit) {
        return jpaQueryFactory
                .select(Projections.constructor(
                        InquirySummaryViewProjection.class,
                        inquiry.id,
                        inquiry.status,
                        inquiry.title,
                        inquiry.createdAt,
                        inquiry.updatedAt
                ))
                .from(inquiry)
                .where(
                        inquiry.memberId.eq(memberId),
                        afterCursor(cursorId)
                )
                .orderBy(inquiry.id.desc())
                .limit(limit)
                .fetch();
    }
    
    private BooleanExpression afterCursor(Long cursorId) {
        if (cursorId == null) {
            return null;
        }
        
        return inquiry.id.lt(cursorId);
    }
}
