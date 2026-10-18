package com.todayyu.persistence.scrap.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.todayyu.persistence.scrap.entity.QScrapJpaEntity;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class CustomScrapRepositoryImpl implements CustomScrapRepository {
    private final JPAQueryFactory jpaQueryFactory;
    
    private final QScrapJpaEntity scrap = QScrapJpaEntity.scrapJpaEntity;
    
    @Override
    public List<Long> findInfoIdsByMemberIdAndInfoIds(Long memberId, List<Long> infoIds) {
        return jpaQueryFactory
                .select(scrap.infoId)
                .from(scrap)
                .where(
                        scrap.memberId.eq(memberId),
                        scrap.infoId.in(infoIds)
                )
                .fetch();
    }
}
