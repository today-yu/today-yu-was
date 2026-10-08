package com.todayyu.persistence.info.mapper;

import com.todayyu.domain.info.entity.InfoViewCount;
import com.todayyu.domain.info.vo.InfoId;
import com.todayyu.domain.info.vo.ViewCount;
import com.todayyu.persistence.info.entity.InfoViewCountJpaEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InfoViewCountMapperTest {
    private static final Long INFO_ID = 100L;
    private static final Long VIEW_COUNT = 42L;

    private final InfoViewCountMapper mapper = new InfoViewCountMapper();

    @Test
    @DisplayName("신규 조회수를 조회수가 0인 신규 JPA 엔티티로 변환한다")
    void toJpaEntity_init() {
        // given
        InfoViewCount infoViewCount = InfoViewCount.init(new InfoId(INFO_ID));

        // when
        InfoViewCountJpaEntity jpaEntity = mapper.toJpaEntity(infoViewCount);

        // then
        assertEquals(INFO_ID, jpaEntity.getInfoId());
        assertEquals(0L, jpaEntity.getViewCount());
        assertTrue(jpaEntity.isNew());
    }

    @Test
    @DisplayName("조회수를 같은 정보 ID와 조회수를 가진 JPA 엔티티로 변환한다")
    void toJpaEntity() {
        // given
        InfoViewCount infoViewCount = InfoViewCount.of(new InfoId(INFO_ID), new ViewCount(VIEW_COUNT));

        // when
        InfoViewCountJpaEntity jpaEntity = mapper.toJpaEntity(infoViewCount);

        // then
        assertEquals(INFO_ID, jpaEntity.getId());
        assertEquals(INFO_ID, jpaEntity.getInfoId());
        assertEquals(VIEW_COUNT, jpaEntity.getViewCount());
    }

    @Test
    @DisplayName("조회수 JPA 엔티티를 같은 정보 ID와 조회수를 가진 조회수로 변환한다")
    void toDomain() {
        // given
        InfoViewCountJpaEntity jpaEntity = InfoViewCountJpaEntity.builder()
                .infoId(INFO_ID)
                .viewCount(VIEW_COUNT)
                .build();

        // when
        InfoViewCount infoViewCount = mapper.toDomain(jpaEntity);

        // then
        assertEquals(new InfoId(INFO_ID), infoViewCount.getInfoId());
        assertEquals(new ViewCount(VIEW_COUNT), infoViewCount.getViewCount());
    }
}
