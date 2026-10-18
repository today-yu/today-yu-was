package com.todayyu.persistence.notice.mapper;

import com.todayyu.domain.notice.entity.Notice;
import com.todayyu.domain.notice.vo.NoticeContent;
import com.todayyu.domain.notice.vo.NoticeId;
import com.todayyu.domain.notice.vo.NoticeTitle;
import com.todayyu.persistence.notice.entity.NoticeJpaEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class NoticeMapperTest {
    private static final Long NOTICE_ID = 1L;
    private static final String TITLE = "서비스 점검 안내";
    private static final String CONTENT = "10월 20일 오전 2시부터 4시까지 서비스 점검이 진행됩니다.";
    private static final LocalDateTime PUBLISHED_AT = LocalDateTime.of(2026, 10, 18, 9, 0);
    private static final LocalDateTime UPDATED_AT = LocalDateTime.of(2026, 10, 19, 18, 30);

    private final NoticeMapper noticeMapper = new NoticeMapper();

    @Test
    @DisplayName("신규 공지를 아이디가 없는 공지 JPA 엔티티로 변환한다")
    void toJpaEntity() {
        // given
        Notice notice = Notice.withoutId(new NoticeTitle(TITLE), new NoticeContent(CONTENT), PUBLISHED_AT);

        // when
        NoticeJpaEntity jpaEntity = noticeMapper.toJpaEntity(notice);

        // then
        assertNull(jpaEntity.getId());
        assertEquals(TITLE, jpaEntity.getTitle());
        assertEquals(CONTENT, jpaEntity.getContent());
        assertEquals(PUBLISHED_AT, jpaEntity.getPublishedAt());
        assertEquals(PUBLISHED_AT, jpaEntity.getUpdatedAt());
    }

    @Test
    @DisplayName("공지 JPA 엔티티를 아이디를 가진 공지로 변환한다")
    void toDomain() {
        // given
        NoticeJpaEntity jpaEntity = NoticeJpaEntity.builder()
                .title(TITLE)
                .content(CONTENT)
                .publishedAt(PUBLISHED_AT)
                .updatedAt(UPDATED_AT)
                .build();
        ReflectionTestUtils.setField(jpaEntity, "id", NOTICE_ID);

        // when
        Notice notice = noticeMapper.toDomain(jpaEntity);

        // then
        assertEquals(new NoticeId(NOTICE_ID), notice.getId());
        assertEquals(new NoticeTitle(TITLE), notice.getTitle());
        assertEquals(new NoticeContent(CONTENT), notice.getContent());
        assertEquals(PUBLISHED_AT, notice.getPublishedAt());
        assertEquals(UPDATED_AT, notice.getUpdatedAt());
    }
}
