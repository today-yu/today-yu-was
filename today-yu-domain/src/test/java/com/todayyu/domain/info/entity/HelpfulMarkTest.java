package com.todayyu.domain.info.entity;

import com.todayyu.domain.info.vo.HelpfulMarkId;
import com.todayyu.domain.info.vo.InfoId;
import com.todayyu.domain.member.vo.MemberId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class HelpfulMarkTest {

    private static final HelpfulMarkId HELPFUL_MARK_ID = new HelpfulMarkId(1L);
    private static final InfoId INFO_ID = new InfoId(1L);
    private static final MemberId MEMBER_ID = new MemberId(1L);

    @Test
    @DisplayName("아이디가 없는 신규 '도움이 됐어요'를 생성할 수 있다")
    void createWithoutId() {
        // when
        HelpfulMark helpfulMark = HelpfulMark.withoutId(INFO_ID, MEMBER_ID);

        // then
        assertNull(helpfulMark.getId());
        assertEquals(INFO_ID, helpfulMark.getInfoId());
        assertEquals(MEMBER_ID, helpfulMark.getMemberId());
    }

    @Test
    @DisplayName("정보 아이디 없이 신규 '도움이 됐어요'를 생성할 수 없다")
    void createWithoutIdWithoutInfoId() {
        assertThrows(IllegalArgumentException.class,
                () -> HelpfulMark.withoutId(null, MEMBER_ID));
    }

    @Test
    @DisplayName("회원 아이디 없이 신규 '도움이 됐어요'를 생성할 수 없다")
    void createWithoutIdWithoutMemberId() {
        assertThrows(IllegalArgumentException.class,
                () -> HelpfulMark.withoutId(INFO_ID, null));
    }

    @Test
    @DisplayName("저장된 정보로 아이디를 가진 '도움이 됐어요'를 생성할 수 있다")
    void createWithId() {
        // when
        HelpfulMark helpfulMark = HelpfulMark.withId(HELPFUL_MARK_ID, INFO_ID, MEMBER_ID);

        // then
        assertEquals(HELPFUL_MARK_ID, helpfulMark.getId());
        assertEquals(INFO_ID, helpfulMark.getInfoId());
        assertEquals(MEMBER_ID, helpfulMark.getMemberId());
    }

    @Test
    @DisplayName("아이디 없이 저장된 '도움이 됐어요'를 생성할 수 없다")
    void createWithIdWithoutHelpfulMarkId() {
        assertThrows(IllegalArgumentException.class,
                () -> HelpfulMark.withId(null, INFO_ID, MEMBER_ID));
    }

    @Test
    @DisplayName("정보 아이디 없이 저장된 '도움이 됐어요'를 생성할 수 없다")
    void createWithIdWithoutInfoId() {
        assertThrows(IllegalArgumentException.class,
                () -> HelpfulMark.withId(HELPFUL_MARK_ID, null, MEMBER_ID));
    }

    @Test
    @DisplayName("회원 아이디 없이 저장된 '도움이 됐어요'를 생성할 수 없다")
    void createWithIdWithoutMemberId() {
        assertThrows(IllegalArgumentException.class,
                () -> HelpfulMark.withId(HELPFUL_MARK_ID, INFO_ID, null));
    }

    @Test
    @DisplayName("아이디가 같은 '도움이 됐어요'는 정보와 회원이 달라도 같은 '도움이 됐어요'이다")
    void equalsById() {
        HelpfulMark helpfulMark = HelpfulMark.withId(HELPFUL_MARK_ID, INFO_ID, MEMBER_ID);
        HelpfulMark other = HelpfulMark.withId(HELPFUL_MARK_ID, new InfoId(2L), new MemberId(2L));

        assertEquals(helpfulMark, other);
        assertEquals(helpfulMark.hashCode(), other.hashCode());
    }

    @Test
    @DisplayName("아이디가 다른 '도움이 됐어요'는 정보와 회원이 같아도 다른 '도움이 됐어요'이다")
    void notEqualsById() {
        HelpfulMark helpfulMark = HelpfulMark.withId(HELPFUL_MARK_ID, INFO_ID, MEMBER_ID);
        HelpfulMark other = HelpfulMark.withId(new HelpfulMarkId(2L), INFO_ID, MEMBER_ID);

        assertNotEquals(helpfulMark, other);
    }
}
