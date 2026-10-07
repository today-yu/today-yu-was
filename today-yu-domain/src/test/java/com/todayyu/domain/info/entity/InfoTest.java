package com.todayyu.domain.info.entity;

import com.todayyu.domain.info.vo.InfoCategory;
import com.todayyu.domain.info.vo.InfoContent;
import com.todayyu.domain.info.vo.InfoId;
import com.todayyu.domain.info.vo.InfoTitle;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class InfoTest {

    private static final InfoId INFO_ID = new InfoId(1L);
    private static final InfoTitle TITLE = new InfoTitle("수강신청 일정 안내");
    private static final InfoContent CONTENT = new InfoContent("2학기 수강신청은 8월 첫째 주에 진행됩니다.");
    private static final LocalDateTime PUBLISHED_AT = LocalDateTime.of(2026, 10, 1, 9, 0);
    private static final LocalDateTime UPDATED_AT = LocalDateTime.of(2026, 10, 5, 18, 30);

    @Test
    @DisplayName("아이디가 없는 신규 정보를 발행할 수 있다")
    void createWithoutId() {
        // when
        Info info = Info.withoutId(InfoCategory.ACADEMIC, TITLE, CONTENT, PUBLISHED_AT);

        // then
        assertNull(info.getId());
        assertEquals(InfoCategory.ACADEMIC, info.getCategory());
        assertEquals(TITLE, info.getTitle());
        assertEquals(CONTENT, info.getContent());
        assertEquals(PUBLISHED_AT, info.getPublishedAt());
    }

    @Test
    @DisplayName("신규 정보의 수정 시각은 발행 시각과 같다")
    void updatedAtEqualsPublishedAtWhenCreated() {
        Info info = Info.withoutId(InfoCategory.ACADEMIC, TITLE, CONTENT, PUBLISHED_AT);

        assertEquals(PUBLISHED_AT, info.getUpdatedAt());
    }

    @Test
    @DisplayName("카테고리 없이 신규 정보를 발행할 수 없다")
    void createWithoutIdWithoutCategory() {
        assertThrows(IllegalArgumentException.class,
                () -> Info.withoutId(null, TITLE, CONTENT, PUBLISHED_AT));
    }

    @Test
    @DisplayName("저장된 정보로 아이디를 가진 정보를 생성할 수 있다")
    void createWithId() {
        // when
        Info info = Info.withId(INFO_ID, InfoCategory.ACADEMIC, TITLE, CONTENT, PUBLISHED_AT, UPDATED_AT);

        // then
        assertEquals(INFO_ID, info.getId());
        assertEquals(InfoCategory.ACADEMIC, info.getCategory());
        assertEquals(TITLE, info.getTitle());
        assertEquals(CONTENT, info.getContent());
        assertEquals(PUBLISHED_AT, info.getPublishedAt());
        assertEquals(UPDATED_AT, info.getUpdatedAt());
    }

    @Test
    @DisplayName("아이디 없이 저장된 정보를 생성할 수 없다")
    void createWithIdWithoutInfoId() {
        assertThrows(IllegalArgumentException.class,
                () -> Info.withId(null, InfoCategory.ACADEMIC, TITLE, CONTENT, PUBLISHED_AT, UPDATED_AT));
    }

    @Test
    @DisplayName("카테고리 없이 저장된 정보를 생성할 수 없다")
    void createWithIdWithoutCategory() {
        assertThrows(IllegalArgumentException.class,
                () -> Info.withId(INFO_ID, null, TITLE, CONTENT, PUBLISHED_AT, UPDATED_AT));
    }

    @Test
    @DisplayName("아이디가 같은 정보는 내용이 달라도 같은 정보이다")
    void equalsById() {
        Info info = Info.withId(INFO_ID, InfoCategory.ACADEMIC, TITLE, CONTENT, PUBLISHED_AT, UPDATED_AT);
        Info other = Info.withId(
                INFO_ID,
                InfoCategory.WELFARE,
                new InfoTitle("다른 제목"),
                new InfoContent("다른 내용"),
                PUBLISHED_AT,
                PUBLISHED_AT
        );

        assertEquals(info, other);
        assertEquals(info.hashCode(), other.hashCode());
    }

    @Test
    @DisplayName("아이디가 다른 정보는 내용이 같아도 다른 정보이다")
    void notEqualsById() {
        Info info = Info.withId(INFO_ID, InfoCategory.ACADEMIC, TITLE, CONTENT, PUBLISHED_AT, UPDATED_AT);
        Info other = Info.withId(new InfoId(2L), InfoCategory.ACADEMIC, TITLE, CONTENT, PUBLISHED_AT, UPDATED_AT);

        assertNotEquals(info, other);
    }
}
