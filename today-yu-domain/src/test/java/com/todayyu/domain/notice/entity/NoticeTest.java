package com.todayyu.domain.notice.entity;

import com.todayyu.domain.notice.vo.NoticeContent;
import com.todayyu.domain.notice.vo.NoticeId;
import com.todayyu.domain.notice.vo.NoticeTitle;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class NoticeTest {

    private static final NoticeId NOTICE_ID = new NoticeId(1L);
    private static final NoticeTitle TITLE = new NoticeTitle("'오늘의 영대' 서비스 오픈");
    private static final NoticeContent CONTENT = new NoticeContent("안녕하세요. '오늘의 영대' 서비스가 오픈되었습니다.");
    private static final LocalDateTime PUBLISHED_AT = LocalDateTime.of(2026, 9, 16, 9, 0);
    private static final LocalDateTime UPDATED_AT = LocalDateTime.of(2026, 9, 20, 18, 30);

    @Test
    @DisplayName("아이디가 없는 신규 공지를 발행할 수 있다")
    void createWithoutId() {
        // when
        Notice notice = Notice.withoutId(TITLE, CONTENT, PUBLISHED_AT);

        // then
        assertNull(notice.getId());
        assertEquals(TITLE, notice.getTitle());
        assertEquals(CONTENT, notice.getContent());
        assertEquals(PUBLISHED_AT, notice.getPublishedAt());
    }

    @Test
    @DisplayName("신규 공지의 수정 시각은 발행 시각과 같다")
    void updatedAtEqualsPublishedAtWhenCreated() {
        Notice notice = Notice.withoutId(TITLE, CONTENT, PUBLISHED_AT);

        assertEquals(PUBLISHED_AT, notice.getUpdatedAt());
    }

    @Test
    @DisplayName("제목 없이 신규 공지를 발행할 수 없다")
    void createWithoutIdWithoutTitle() {
        assertThrows(IllegalArgumentException.class,
                () -> Notice.withoutId(null, CONTENT, PUBLISHED_AT));
    }

    @Test
    @DisplayName("내용 없이 신규 공지를 발행할 수 없다")
    void createWithoutIdWithoutContent() {
        assertThrows(IllegalArgumentException.class,
                () -> Notice.withoutId(TITLE, null, PUBLISHED_AT));
    }

    @Test
    @DisplayName("발행 시각 없이 신규 공지를 발행할 수 없다")
    void createWithoutIdWithoutPublishedAt() {
        assertThrows(IllegalArgumentException.class,
                () -> Notice.withoutId(TITLE, CONTENT, null));
    }

    @Test
    @DisplayName("저장된 공지로 아이디를 가진 공지를 생성할 수 있다")
    void createWithId() {
        // when
        Notice notice = Notice.withId(NOTICE_ID, TITLE, CONTENT, PUBLISHED_AT, UPDATED_AT);

        // then
        assertEquals(NOTICE_ID, notice.getId());
        assertEquals(TITLE, notice.getTitle());
        assertEquals(CONTENT, notice.getContent());
        assertEquals(PUBLISHED_AT, notice.getPublishedAt());
        assertEquals(UPDATED_AT, notice.getUpdatedAt());
    }

    @Test
    @DisplayName("아이디 없이 저장된 공지를 생성할 수 없다")
    void createWithIdWithoutNoticeId() {
        assertThrows(IllegalArgumentException.class,
                () -> Notice.withId(null, TITLE, CONTENT, PUBLISHED_AT, UPDATED_AT));
    }

    @Test
    @DisplayName("제목 없이 저장된 공지를 생성할 수 없다")
    void createWithIdWithoutTitle() {
        assertThrows(IllegalArgumentException.class,
                () -> Notice.withId(NOTICE_ID, null, CONTENT, PUBLISHED_AT, UPDATED_AT));
    }

    @Test
    @DisplayName("내용 없이 저장된 공지를 생성할 수 없다")
    void createWithIdWithoutContent() {
        assertThrows(IllegalArgumentException.class,
                () -> Notice.withId(NOTICE_ID, TITLE, null, PUBLISHED_AT, UPDATED_AT));
    }

    @Test
    @DisplayName("발행 시각 없이 저장된 공지를 생성할 수 없다")
    void createWithIdWithoutPublishedAt() {
        assertThrows(IllegalArgumentException.class,
                () -> Notice.withId(NOTICE_ID, TITLE, CONTENT, null, UPDATED_AT));
    }

    @Test
    @DisplayName("수정 시각 없이 저장된 공지를 생성할 수 없다")
    void createWithIdWithoutUpdatedAt() {
        assertThrows(IllegalArgumentException.class,
                () -> Notice.withId(NOTICE_ID, TITLE, CONTENT, PUBLISHED_AT, null));
    }

    @Test
    @DisplayName("아이디가 같은 공지는 내용이 달라도 같은 공지이다")
    void equalsById() {
        Notice notice = Notice.withId(NOTICE_ID, TITLE, CONTENT, PUBLISHED_AT, UPDATED_AT);
        Notice other = Notice.withId(
                NOTICE_ID,
                new NoticeTitle("다른 제목"),
                new NoticeContent("다른 내용"),
                PUBLISHED_AT,
                PUBLISHED_AT
        );

        assertEquals(notice, other);
        assertEquals(notice.hashCode(), other.hashCode());
    }

    @Test
    @DisplayName("아이디가 다른 공지는 내용이 같아도 다른 공지이다")
    void notEqualsById() {
        Notice notice = Notice.withId(NOTICE_ID, TITLE, CONTENT, PUBLISHED_AT, UPDATED_AT);
        Notice other = Notice.withId(new NoticeId(2L), TITLE, CONTENT, PUBLISHED_AT, UPDATED_AT);

        assertNotEquals(notice, other);
    }
}
