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
     * @param info {@link Info} 객체
     * @return {@link InfoJpaEntity} 객체
     */
    public InfoJpaEntity toJpaEntity(Info info) {
        return InfoJpaEntity.builder()
                .category(info.getCategory())
                .title(info.getTitle().getValue())
                .content(info.getContent().getValue())
                .publishedAt(info.getPublishedAt())
                .updatedAt(info.getUpdatedAt())
                .build();
    }
    
    /**
     * {@link InfoJpaEntity}를 {@link Info}로 변환하는 메서드
     *
     * @param infoJpaEntity {@link InfoJpaEntity} 객체
     * @return {@link Info} 객체
     */
    public Info toDomain(InfoJpaEntity infoJpaEntity) {
        return Info.withId(
                new InfoId(infoJpaEntity.getId()),
                infoJpaEntity.getCategory(),
                new InfoTitle(infoJpaEntity.getTitle()),
                new InfoContent(infoJpaEntity.getContent()),
                infoJpaEntity.getPublishedAt(),
                infoJpaEntity.getUpdatedAt()
        );
    }
}
