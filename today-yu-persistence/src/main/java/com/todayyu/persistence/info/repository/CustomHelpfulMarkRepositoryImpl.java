package com.todayyu.persistence.info.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.todayyu.persistence.info.dto.HelpfulMarkCountProjection;
import com.todayyu.persistence.info.entity.QHelpfulMarkJpaEntity;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class CustomHelpfulMarkRepositoryImpl implements CustomHelpfulMarkRepository {
    private final JPAQueryFactory jpaQueryFactory;
    
    private final QHelpfulMarkJpaEntity helpfulMark = QHelpfulMarkJpaEntity.helpfulMarkJpaEntity;
    
    @Override
    public List<HelpfulMarkCountProjection> countByInfoIds(List<Long> infoIds) {
        return jpaQueryFactory
                .select(
                        Projections.constructor(
                                HelpfulMarkCountProjection.class,
                                helpfulMark.infoId,
                                helpfulMark.count()
                        )
                )
                .from(helpfulMark)
                .where(helpfulMark.infoId.in(infoIds))
                .groupBy(helpfulMark.infoId)
                .fetch();
    }
}
