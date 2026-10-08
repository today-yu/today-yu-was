package com.todayyu.persistence.info.mapper;

import com.todayyu.domain.info.entity.Info;
import com.todayyu.domain.info.vo.InfoCategory;
import com.todayyu.domain.info.vo.InfoContent;
import com.todayyu.domain.info.vo.InfoId;
import com.todayyu.domain.info.vo.InfoTitle;
import com.todayyu.persistence.info.entity.InfoJpaEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class InfoMapperTest {
    private static final Long INFO_ID = 1L;
    private static final String TITLE = "중앙도서관 운영 시간";
    private static final String CONTENT = "학기 중 중앙도서관은 평일 오전 9시부터 오후 10시까지 운영합니다.";
    private static final LocalDateTime PUBLISHED_AT = LocalDateTime.of(2026, 10, 1, 9, 0);
    private static final LocalDateTime UPDATED_AT = LocalDateTime.of(2026, 10, 5, 18, 30);

    private final InfoMapper infoMapper = new InfoMapper();

    @Test
    @DisplayName("신규 정보를 아이디가 없는 정보 JPA 엔티티로 변환한다")
    void toJpaEntity() {
        // given
        Info info = Info.withoutId(InfoCategory.FACILITY, new InfoTitle(TITLE), new InfoContent(CONTENT), PUBLISHED_AT);

        // when
        InfoJpaEntity jpaEntity = infoMapper.toJpaEntity(info);

        // then
        assertNull(jpaEntity.getId());
        assertEquals(InfoCategory.FACILITY, jpaEntity.getCategory());
        assertEquals(TITLE, jpaEntity.getTitle());
        assertEquals(CONTENT, jpaEntity.getContent());
        assertEquals(PUBLISHED_AT, jpaEntity.getPublishedAt());
        assertEquals(PUBLISHED_AT, jpaEntity.getUpdatedAt());
    }

    @Test
    @DisplayName("정보 JPA 엔티티를 아이디를 가진 정보로 변환한다")
    void toDomain() {
        // given
        InfoJpaEntity jpaEntity = InfoJpaEntity.builder()
                .category(InfoCategory.FACILITY)
                .title(TITLE)
                .content(CONTENT)
                .publishedAt(PUBLISHED_AT)
                .updatedAt(UPDATED_AT)
                .build();
        ReflectionTestUtils.setField(jpaEntity, "id", INFO_ID);

        // when
        Info info = infoMapper.toDomain(jpaEntity);

        // then
        assertEquals(new InfoId(INFO_ID), info.getId());
        assertEquals(InfoCategory.FACILITY, info.getCategory());
        assertEquals(new InfoTitle(TITLE), info.getTitle());
        assertEquals(new InfoContent(CONTENT), info.getContent());
        assertEquals(PUBLISHED_AT, info.getPublishedAt());
        assertEquals(UPDATED_AT, info.getUpdatedAt());
    }
}
