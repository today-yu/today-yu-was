package com.todayyu.persistence.scrap.mapper;

import com.todayyu.domain.info.vo.InfoId;
import com.todayyu.domain.member.vo.MemberId;
import com.todayyu.domain.scrap.entity.Scrap;
import com.todayyu.domain.scrap.vo.ScrapId;
import com.todayyu.persistence.scrap.entity.ScrapJpaEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ScrapMapperTest {
    private static final Long SCRAP_ID = 1L;
    private static final Long INFO_ID = 100L;
    private static final Long MEMBER_ID = 10L;
    private static final LocalDateTime SCRAPPED_AT = LocalDateTime.of(2026, 10, 18, 12, 0);

    private final ScrapMapper mapper = new ScrapMapper();

    @Test
    @DisplayName("신규 스크랩을 아이디가 없는 JPA 엔티티로 변환한다")
    void toJpaEntity() {
        // given
        Scrap scrap = Scrap.withoutId(new InfoId(INFO_ID), new MemberId(MEMBER_ID), SCRAPPED_AT);

        // when
        ScrapJpaEntity jpaEntity = mapper.toJpaEntity(scrap);

        // then
        assertNull(jpaEntity.getId());
        assertEquals(INFO_ID, jpaEntity.getInfoId());
        assertEquals(MEMBER_ID, jpaEntity.getMemberId());
        assertEquals(SCRAPPED_AT, jpaEntity.getScrappedAt());
    }

    @Test
    @DisplayName("스크랩 JPA 엔티티를 아이디를 가진 스크랩으로 변환한다")
    void toDomain() {
        // given
        ScrapJpaEntity jpaEntity = ScrapJpaEntity.builder()
                .infoId(INFO_ID)
                .memberId(MEMBER_ID)
                .scrappedAt(SCRAPPED_AT)
                .build();
        ReflectionTestUtils.setField(jpaEntity, "id", SCRAP_ID);

        // when
        Scrap scrap = mapper.toDomain(jpaEntity);

        // then
        assertEquals(new ScrapId(SCRAP_ID), scrap.getId());
        assertEquals(new InfoId(INFO_ID), scrap.getInfoId());
        assertEquals(new MemberId(MEMBER_ID), scrap.getMemberId());
        assertEquals(SCRAPPED_AT, scrap.getScrappedAt());
    }
}
