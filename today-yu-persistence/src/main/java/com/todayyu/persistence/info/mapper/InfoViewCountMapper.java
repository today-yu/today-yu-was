package com.todayyu.persistence.info.mapper;

import com.todayyu.domain.info.entity.InfoViewCount;
import com.todayyu.domain.info.vo.InfoId;
import com.todayyu.domain.info.vo.ViewCount;
import com.todayyu.persistence.common.Mapper;
import com.todayyu.persistence.info.entity.InfoViewCountJpaEntity;

@Mapper
public class InfoViewCountMapper {
    /**
     * {@link InfoViewCount}를 {@link InfoViewCountJpaEntity}로 변환하는 메서드
     *
     * @param domainEntity {@link InfoViewCount} 객체
     * @return {@link InfoViewCountJpaEntity} 객체
     */
    public InfoViewCountJpaEntity toJpaEntity(InfoViewCount domainEntity) {
        return InfoViewCountJpaEntity.builder()
                .infoId(domainEntity.getInfoId().getValue())
                .viewCount(domainEntity.getViewCount().getValue())
                .build();
    }

    /**
     * {@link InfoViewCountJpaEntity}를 {@link InfoViewCount}로 변환하는 메서드
     *
     * @param jpaEntity {@link InfoViewCountJpaEntity} 객체
     * @return {@link InfoViewCount} 객체
     */
    public InfoViewCount toDomain(InfoViewCountJpaEntity jpaEntity) {
        return InfoViewCount.of(
                new InfoId(jpaEntity.getInfoId()),
                new ViewCount(jpaEntity.getViewCount())
        );
    }
}