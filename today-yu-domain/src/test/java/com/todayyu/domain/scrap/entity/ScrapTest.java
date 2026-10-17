package com.todayyu.domain.scrap.entity;

import com.todayyu.domain.info.vo.InfoId;
import com.todayyu.domain.member.vo.MemberId;
import com.todayyu.domain.scrap.vo.ScrapId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ScrapTest {
    private static final ScrapId SCRAP_ID = new ScrapId(1L);
    private static final InfoId INFO_ID = new InfoId(1L);
    private static final MemberId MEMBER_ID = new MemberId(1L);
    private static final LocalDateTime SCRAPPED_AT = LocalDateTime.of(2026, 10, 17, 12, 0);

    @Test
    @DisplayName("아이디가 없는 신규 스크랩을 생성할 수 있다")
    void createWithoutId() {
        // when
        Scrap scrap = Scrap.withoutId(INFO_ID, MEMBER_ID, SCRAPPED_AT);

        // then
        assertNull(scrap.getId());
        assertEquals(INFO_ID, scrap.getInfoId());
        assertEquals(MEMBER_ID, scrap.getMemberId());
        assertEquals(SCRAPPED_AT, scrap.getScrappedAt());
    }

    @Test
    @DisplayName("정보 아이디 없이 신규 스크랩을 생성할 수 없다")
    void createWithoutIdWithoutInfoId() {
        assertThrows(IllegalArgumentException.class,
                () -> Scrap.withoutId(null, MEMBER_ID, SCRAPPED_AT));
    }

    @Test
    @DisplayName("회원 아이디 없이 신규 스크랩을 생성할 수 없다")
    void createWithoutIdWithoutMemberId() {
        assertThrows(IllegalArgumentException.class,
                () -> Scrap.withoutId(INFO_ID, null, SCRAPPED_AT));
    }

    @Test
    @DisplayName("스크랩 시각 없이 신규 스크랩을 생성할 수 없다")
    void createWithoutIdWithoutScrappedAt() {
        assertThrows(IllegalArgumentException.class,
                () -> Scrap.withoutId(INFO_ID, MEMBER_ID, null));
    }

    @Test
    @DisplayName("저장된 정보로 아이디를 가진 스크랩을 생성할 수 있다")
    void createWithId() {
        // when
        Scrap scrap = Scrap.withId(SCRAP_ID, INFO_ID, MEMBER_ID, SCRAPPED_AT);

        // then
        assertEquals(SCRAP_ID, scrap.getId());
        assertEquals(INFO_ID, scrap.getInfoId());
        assertEquals(MEMBER_ID, scrap.getMemberId());
        assertEquals(SCRAPPED_AT, scrap.getScrappedAt());
    }

    @Test
    @DisplayName("아이디 없이 저장된 스크랩을 생성할 수 없다")
    void createWithIdWithoutScrapId() {
        assertThrows(IllegalArgumentException.class,
                () -> Scrap.withId(null, INFO_ID, MEMBER_ID, SCRAPPED_AT));
    }

    @Test
    @DisplayName("정보 아이디 없이 저장된 스크랩을 생성할 수 없다")
    void createWithIdWithoutInfoId() {
        assertThrows(IllegalArgumentException.class,
                () -> Scrap.withId(SCRAP_ID, null, MEMBER_ID, SCRAPPED_AT));
    }

    @Test
    @DisplayName("회원 아이디 없이 저장된 스크랩을 생성할 수 없다")
    void createWithIdWithoutMemberId() {
        assertThrows(IllegalArgumentException.class,
                () -> Scrap.withId(SCRAP_ID, INFO_ID, null, SCRAPPED_AT));
    }

    @Test
    @DisplayName("스크랩 시각 없이 저장된 스크랩을 생성할 수 없다")
    void createWithIdWithoutScrappedAt() {
        assertThrows(IllegalArgumentException.class,
                () -> Scrap.withId(SCRAP_ID, INFO_ID, MEMBER_ID, null));
    }

    @Test
    @DisplayName("아이디가 같은 스크랩은 정보와 회원, 스크랩 시각이 달라도 같은 스크랩이다")
    void equalsById() {
        Scrap scrap = Scrap.withId(SCRAP_ID, INFO_ID, MEMBER_ID, SCRAPPED_AT);
        Scrap other = Scrap.withId(SCRAP_ID, new InfoId(2L), new MemberId(2L), SCRAPPED_AT.plusDays(1));

        assertEquals(scrap, other);
        assertEquals(scrap.hashCode(), other.hashCode());
    }

    @Test
    @DisplayName("아이디가 다른 스크랩은 정보와 회원, 스크랩 시각이 같아도 다른 스크랩이다")
    void notEqualsById() {
        Scrap scrap = Scrap.withId(SCRAP_ID, INFO_ID, MEMBER_ID, SCRAPPED_AT);
        Scrap other = Scrap.withId(new ScrapId(2L), INFO_ID, MEMBER_ID, SCRAPPED_AT);

        assertNotEquals(scrap, other);
    }
}
