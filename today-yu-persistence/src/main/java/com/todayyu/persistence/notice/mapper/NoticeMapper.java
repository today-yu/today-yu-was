package com.todayyu.persistence.notice.mapper;

import com.todayyu.domain.notice.entity.Notice;
import com.todayyu.domain.notice.vo.NoticeContent;
import com.todayyu.domain.notice.vo.NoticeId;
import com.todayyu.domain.notice.vo.NoticeTitle;
import com.todayyu.persistence.common.Mapper;
import com.todayyu.persistence.notice.entity.NoticeJpaEntity;

@Mapper
public class NoticeMapper {
    /**
     * {@link Notice}를 {@link NoticeJpaEntity}로 변환하는 메서드
     *
     * @param domainEntity {@link Notice} 객체
     * @return {@link NoticeJpaEntity} 객체
     */
    public NoticeJpaEntity toJpaEntity(Notice domainEntity) {
        return NoticeJpaEntity.builder()
                .title(domainEntity.getTitle().getValue())
                .content(domainEntity.getContent().getValue())
                .publishedAt(domainEntity.getPublishedAt())
                .updatedAt(domainEntity.getUpdatedAt())
                .build();
    }

    /**
     * {@link NoticeJpaEntity}를 {@link Notice}로 변환하는 메서드
     *
     * @param jpaEntity {@link NoticeJpaEntity} 객체
     * @return {@link Notice} 객체
     */
    public Notice toDomain(NoticeJpaEntity jpaEntity) {
        return Notice.withId(
                new NoticeId(jpaEntity.getId()),
                new NoticeTitle(jpaEntity.getTitle()),
                new NoticeContent(jpaEntity.getContent()),
                jpaEntity.getPublishedAt(),
                jpaEntity.getUpdatedAt()
        );
    }
}
