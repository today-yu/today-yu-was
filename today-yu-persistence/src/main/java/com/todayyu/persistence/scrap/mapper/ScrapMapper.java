package com.todayyu.persistence.scrap.mapper;

import com.todayyu.domain.info.vo.InfoId;
import com.todayyu.domain.member.vo.MemberId;
import com.todayyu.domain.scrap.entity.Scrap;
import com.todayyu.domain.scrap.vo.ScrapId;
import com.todayyu.persistence.common.Mapper;
import com.todayyu.persistence.scrap.entity.ScrapJpaEntity;

@Mapper
public class ScrapMapper {
    /**
     * {@link Scrap}을 {@link ScrapJpaEntity}로 변환하는 메서드
     *
     * @param domainEntity {@link Scrap} 객체
     * @return {@link ScrapJpaEntity} 객체
     */
    public ScrapJpaEntity toJpaEntity(Scrap domainEntity) {
        return ScrapJpaEntity.builder()
                .infoId(domainEntity.getInfoId().getValue())
                .memberId(domainEntity.getMemberId().getValue())
                .scrappedAt(domainEntity.getScrappedAt())
                .build();
    }

    /**
     * {@link ScrapJpaEntity}를 {@link Scrap}으로 변환하는 메서드
     *
     * @param jpaEntity {@link ScrapJpaEntity} 객체
     * @return {@link Scrap} 객체
     */
    public Scrap toDomain(ScrapJpaEntity jpaEntity) {
        return Scrap.withId(
                new ScrapId(jpaEntity.getId()),
                new InfoId(jpaEntity.getInfoId()),
                new MemberId(jpaEntity.getMemberId()),
                jpaEntity.getScrappedAt()
        );
    }
}
