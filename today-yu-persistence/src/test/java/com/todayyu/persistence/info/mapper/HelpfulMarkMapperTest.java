package com.todayyu.persistence.info.mapper;

import com.todayyu.domain.info.entity.HelpfulMark;
import com.todayyu.domain.info.vo.HelpfulMarkId;
import com.todayyu.domain.info.vo.InfoId;
import com.todayyu.domain.member.vo.MemberId;
import com.todayyu.persistence.info.entity.HelpfulMarkJpaEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class HelpfulMarkMapperTest {
    private static final Long HELPFUL_MARK_ID = 1L;
    private static final Long INFO_ID = 100L;
    private static final Long MEMBER_ID = 10L;

    private final HelpfulMarkMapper mapper = new HelpfulMarkMapper();

    @Test
    @DisplayName("신규 '도움이 되는 정보에요'를 아이디가 없는 JPA 엔티티로 변환한다")
    void toJpaEntity() {
        // given
        HelpfulMark helpfulMark = HelpfulMark.withoutId(new InfoId(INFO_ID), new MemberId(MEMBER_ID));

        // when
        HelpfulMarkJpaEntity jpaEntity = mapper.toJpaEntity(helpfulMark);

        // then
        assertNull(jpaEntity.getId());
        assertEquals(INFO_ID, jpaEntity.getInfoId());
        assertEquals(MEMBER_ID, jpaEntity.getMemberId());
    }

    @Test
    @DisplayName("'도움이 되는 정보에요' JPA 엔티티를 아이디를 가진 '도움이 되는 정보에요'로 변환한다")
    void toDomain() {
        // given
        HelpfulMarkJpaEntity jpaEntity = HelpfulMarkJpaEntity.builder()
                .infoId(INFO_ID)
                .memberId(MEMBER_ID)
                .build();
        ReflectionTestUtils.setField(jpaEntity, "id", HELPFUL_MARK_ID);

        // when
        HelpfulMark helpfulMark = mapper.toDomain(jpaEntity);

        // then
        assertEquals(new HelpfulMarkId(HELPFUL_MARK_ID), helpfulMark.getId());
        assertEquals(new InfoId(INFO_ID), helpfulMark.getInfoId());
        assertEquals(new MemberId(MEMBER_ID), helpfulMark.getMemberId());
    }
}
