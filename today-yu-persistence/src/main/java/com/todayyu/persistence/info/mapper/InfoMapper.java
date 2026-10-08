package com.todayyu.persistence.info.mapper;

import com.todayyu.domain.info.entity.Info;
import com.todayyu.domain.info.vo.InfoContent;
import com.todayyu.domain.info.vo.InfoId;
import com.todayyu.domain.info.vo.InfoTitle;
import com.todayyu.persistence.common.Mapper;
import com.todayyu.persistence.info.entity.InfoJpaEntity;

@Mapper
public class InfoMapper {
    /**
     * {@link Info}를 {@link InfoJpaEntity}로 변환하는 메서드
     *
     * @param domainEntity {@link Info} 객체
     * @return {@link InfoJpaEntity} 객체
     */
    public InfoJpaEntity toJpaEntity(Info domainEntity) {
        return InfoJpaEntity.builder()
                .category(domainEntity.getCategory())
                .title(domainEntity.getTitle().getValue())
                .content(domainEntity.getContent().getValue())
                .publishedAt(domainEntity.getPublishedAt())
                .updatedAt(domainEntity.getUpdatedAt())
                .build();
    }
    
    /**
     * {@link InfoJpaEntity}를 {@link Info}로 변환하는 메서드
     *
     * @param jpaEntity {@link InfoJpaEntity} 객체
     * @return {@link Info} 객체
     */
    public Info toDomain(InfoJpaEntity jpaEntity) {
        return Info.withId(
                new InfoId(jpaEntity.getId()),
                jpaEntity.getCategory(),
                new InfoTitle(jpaEntity.getTitle()),
                new InfoContent(jpaEntity.getContent()),
                jpaEntity.getPublishedAt(),
                jpaEntity.getUpdatedAt()
        );
    }
}
