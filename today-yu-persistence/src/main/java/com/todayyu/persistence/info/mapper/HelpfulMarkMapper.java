package com.todayyu.persistence.info.mapper;

import com.todayyu.domain.info.entity.HelpfulMark;
import com.todayyu.domain.info.vo.HelpfulMarkId;
import com.todayyu.domain.info.vo.InfoId;
import com.todayyu.domain.member.vo.MemberId;
import com.todayyu.persistence.common.Mapper;
import com.todayyu.persistence.info.entity.HelpfulMarkJpaEntity;

@Mapper
public class HelpfulMarkMapper {
    /**
     * {@link HelpfulMark}를 {@link HelpfulMarkJpaEntity}로 변환하는 메서드
     *
     * @param domainEntity {@link HelpfulMark} 객체
     * @return {@link HelpfulMarkJpaEntity} 객체
     */
    public HelpfulMarkJpaEntity toJpaEntity(HelpfulMark domainEntity) {
        return HelpfulMarkJpaEntity.builder()
                .infoId(domainEntity.getInfoId().getValue())
                .memberId(domainEntity.getMemberId().getValue())
                .build();
    }

    /**
     * {@link HelpfulMarkJpaEntity}를 {@link HelpfulMark}로 변환하는 메서드
     *
     * @param jpaEntity {@link HelpfulMarkJpaEntity} 객체
     * @return {@link HelpfulMark} 객체
     */
    public HelpfulMark toDomain(HelpfulMarkJpaEntity jpaEntity) {
        return HelpfulMark.withId(
                new HelpfulMarkId(jpaEntity.getId()),
                new InfoId(jpaEntity.getInfoId()),
                new MemberId(jpaEntity.getMemberId())
        );
    }
}
