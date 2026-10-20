package com.todayyu.domain.inquiry.entity;

import com.todayyu.domain.common.vo.Email;
import com.todayyu.domain.inquiry.vo.InquiryContent;
import com.todayyu.domain.inquiry.vo.InquiryId;
import com.todayyu.domain.inquiry.vo.InquiryStatus;
import com.todayyu.domain.inquiry.vo.InquiryTitle;
import com.todayyu.domain.member.vo.MemberId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDateTime;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InquiryTest {
    private static final InquiryId INQUIRY_ID = new InquiryId(1L);
    private static final MemberId MEMBER_ID = new MemberId(1L);
    private static final Email CONTACT_EMAIL = new Email("test@yu.ac.kr");
    private static final InquiryTitle TITLE = new InquiryTitle("정보 등록의 기준이 궁금합니다.");
    private static final InquiryContent CONTENT = new InquiryContent("정보 등록은 어떤 기준으로 진행되는 것인지 궁금합니다.");
    private static final LocalDateTime CREATED_AT = LocalDateTime.of(2026, 10, 1, 9, 0);
    private static final LocalDateTime UPDATED_AT = LocalDateTime.of(2026, 10, 2, 9, 0);
    private static final LocalDateTime ANSWERED_AT = LocalDateTime.of(2026, 10, 3, 9, 0);

    @Test
    @DisplayName("아이디가 없는 신규 문의를 생성할 수 있다")
    void createWithoutId() {
        // when
        Inquiry inquiry = Inquiry.withoutId(MEMBER_ID, CONTACT_EMAIL, TITLE, CONTENT, CREATED_AT);

        // then
        assertNull(inquiry.getId());
        assertEquals(MEMBER_ID, inquiry.getMemberId());
        assertEquals(CONTACT_EMAIL, inquiry.getContactEmail());
        assertEquals(TITLE, inquiry.getTitle());
        assertEquals(CONTENT, inquiry.getContent());
        assertEquals(CREATED_AT, inquiry.getCreatedAt());
    }

    @Test
    @DisplayName("신규 문의는 답변 대기 중 상태이며 수정 시각은 작성 시각과 같고 답변 시각이 없다")
    void waitingWhenCreated() {
        Inquiry inquiry = Inquiry.withoutId(MEMBER_ID, CONTACT_EMAIL, TITLE, CONTENT, CREATED_AT);

        assertEquals(InquiryStatus.WAITING, inquiry.getStatus());
        assertEquals(CREATED_AT, inquiry.getUpdatedAt());
        assertNull(inquiry.getAnsweredAt());
    }

    @Test
    @DisplayName("작성 회원 없이 신규 문의를 생성할 수 없다")
    void createWithoutIdWithoutMemberId() {
        assertThrows(IllegalArgumentException.class,
                () -> Inquiry.withoutId(null, CONTACT_EMAIL, TITLE, CONTENT, CREATED_AT));
    }

    @Test
    @DisplayName("연락용 이메일 없이 신규 문의를 생성할 수 없다")
    void createWithoutIdWithoutContactEmail() {
        assertThrows(IllegalArgumentException.class,
                () -> Inquiry.withoutId(MEMBER_ID, null, TITLE, CONTENT, CREATED_AT));
    }

    @Test
    @DisplayName("문의 제목 없이 신규 문의를 생성할 수 없다")
    void createWithoutIdWithoutTitle() {
        assertThrows(IllegalArgumentException.class,
                () -> Inquiry.withoutId(MEMBER_ID, CONTACT_EMAIL, null, CONTENT, CREATED_AT));
    }

    @Test
    @DisplayName("문의 내용 없이 신규 문의를 생성할 수 없다")
    void createWithoutIdWithoutContent() {
        assertThrows(IllegalArgumentException.class,
                () -> Inquiry.withoutId(MEMBER_ID, CONTACT_EMAIL, TITLE, null, CREATED_AT));
    }

    @Test
    @DisplayName("작성 시각 없이 신규 문의를 생성할 수 없다")
    void createWithoutIdWithoutCreatedAt() {
        assertThrows(IllegalArgumentException.class,
                () -> Inquiry.withoutId(MEMBER_ID, CONTACT_EMAIL, TITLE, CONTENT, null));
    }

    @Test
    @DisplayName("저장된 문의로 아이디를 가진 문의를 생성할 수 있다")
    void createWithId() {
        // when
        Inquiry inquiry = Inquiry.withId(
                INQUIRY_ID, MEMBER_ID, CONTACT_EMAIL, InquiryStatus.ANSWERED, TITLE, CONTENT,
                CREATED_AT, UPDATED_AT, ANSWERED_AT);

        // then
        assertEquals(INQUIRY_ID, inquiry.getId());
        assertEquals(MEMBER_ID, inquiry.getMemberId());
        assertEquals(CONTACT_EMAIL, inquiry.getContactEmail());
        assertEquals(InquiryStatus.ANSWERED, inquiry.getStatus());
        assertEquals(TITLE, inquiry.getTitle());
        assertEquals(CONTENT, inquiry.getContent());
        assertEquals(CREATED_AT, inquiry.getCreatedAt());
        assertEquals(UPDATED_AT, inquiry.getUpdatedAt());
        assertEquals(ANSWERED_AT, inquiry.getAnsweredAt());
    }

    @Test
    @DisplayName("아이디 없이 저장된 문의를 생성할 수 없다")
    void createWithIdWithoutInquiryId() {
        assertThrows(IllegalArgumentException.class,
                () -> Inquiry.withId(
                        null, MEMBER_ID, CONTACT_EMAIL, InquiryStatus.WAITING, TITLE, CONTENT,
                        CREATED_AT, UPDATED_AT, null));
    }

    @Test
    @DisplayName("작성 회원 없이 저장된 문의를 생성할 수 없다")
    void createWithIdWithoutMemberId() {
        assertThrows(IllegalArgumentException.class,
                () -> Inquiry.withId(
                        INQUIRY_ID, null, CONTACT_EMAIL, InquiryStatus.WAITING, TITLE, CONTENT,
                        CREATED_AT, UPDATED_AT, null));
    }

    @Test
    @DisplayName("연락용 이메일 없이 저장된 문의를 생성할 수 없다")
    void createWithIdWithoutContactEmail() {
        assertThrows(IllegalArgumentException.class,
                () -> Inquiry.withId(
                        INQUIRY_ID, MEMBER_ID, null, InquiryStatus.WAITING, TITLE, CONTENT,
                        CREATED_AT, UPDATED_AT, null));
    }

    @Test
    @DisplayName("상태 없이 저장된 문의를 생성할 수 없다")
    void createWithIdWithoutStatus() {
        assertThrows(IllegalArgumentException.class,
                () -> Inquiry.withId(
                        INQUIRY_ID, MEMBER_ID, CONTACT_EMAIL, null, TITLE, CONTENT,
                        CREATED_AT, UPDATED_AT, null));
    }

    @Test
    @DisplayName("문의 제목 없이 저장된 문의를 생성할 수 없다")
    void createWithIdWithoutTitle() {
        assertThrows(IllegalArgumentException.class,
                () -> Inquiry.withId(
                        INQUIRY_ID, MEMBER_ID, CONTACT_EMAIL, InquiryStatus.WAITING, null, CONTENT,
                        CREATED_AT, UPDATED_AT, null));
    }

    @Test
    @DisplayName("문의 내용 없이 저장된 문의를 생성할 수 없다")
    void createWithIdWithoutContent() {
        assertThrows(IllegalArgumentException.class,
                () -> Inquiry.withId(
                        INQUIRY_ID, MEMBER_ID, CONTACT_EMAIL, InquiryStatus.WAITING, TITLE, null,
                        CREATED_AT, UPDATED_AT, null));
    }

    @Test
    @DisplayName("작성 시각 없이 저장된 문의를 생성할 수 없다")
    void createWithIdWithoutCreatedAt() {
        assertThrows(IllegalArgumentException.class,
                () -> Inquiry.withId(
                        INQUIRY_ID, MEMBER_ID, CONTACT_EMAIL, InquiryStatus.WAITING, TITLE, CONTENT,
                        null, UPDATED_AT, null));
    }

    @Test
    @DisplayName("수정 시각 없이 저장된 문의를 생성할 수 없다")
    void createWithIdWithoutUpdatedAt() {
        assertThrows(IllegalArgumentException.class,
                () -> Inquiry.withId(
                        INQUIRY_ID, MEMBER_ID, CONTACT_EMAIL, InquiryStatus.WAITING, TITLE, CONTENT,
                        CREATED_AT, null, null));
    }

    static Stream<Arguments> validStatusCombinations() {
        return Stream.of(
                Arguments.of(InquiryStatus.WAITING, null),
                Arguments.of(InquiryStatus.ANSWERED, ANSWERED_AT)
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("validStatusCombinations")
    @DisplayName("상태에 맞는 답변 시각으로 저장된 문의를 생성할 수 있다")
    void createWithIdWithValidStatus(InquiryStatus status, LocalDateTime answeredAt) {
        assertDoesNotThrow(() -> Inquiry.withId(
                INQUIRY_ID, MEMBER_ID, CONTACT_EMAIL, status, TITLE, CONTENT,
                CREATED_AT, UPDATED_AT, answeredAt));
    }

    static Stream<Arguments> invalidStatusCombinations() {
        return Stream.of(
                Arguments.of(InquiryStatus.WAITING, ANSWERED_AT),
                Arguments.of(InquiryStatus.ANSWERED, null)
        );
    }

    @ParameterizedTest(name = "{0} - 답변 시각: {1}")
    @MethodSource("invalidStatusCombinations")
    @DisplayName("상태에 맞지 않는 답변 시각으로 저장된 문의를 생성할 수 없다")
    void createWithIdWithInvalidStatus(InquiryStatus status, LocalDateTime answeredAt) {
        assertThrows(IllegalArgumentException.class,
                () -> Inquiry.withId(
                        INQUIRY_ID, MEMBER_ID, CONTACT_EMAIL, status, TITLE, CONTENT,
                        CREATED_AT, UPDATED_AT, answeredAt));
    }

    @Test
    @DisplayName("아이디가 같은 문의는 내용이 달라도 같은 문의이다")
    void equalsById() {
        Inquiry inquiry = Inquiry.withId(
                INQUIRY_ID, MEMBER_ID, CONTACT_EMAIL, InquiryStatus.WAITING, TITLE, CONTENT,
                CREATED_AT, UPDATED_AT, null);
        Inquiry other = Inquiry.withId(
                INQUIRY_ID, new MemberId(2L), new Email("other@yu.ac.kr"), InquiryStatus.ANSWERED,
                new InquiryTitle("다른 문의 제목"), new InquiryContent("다른 문의 내용"),
                CREATED_AT, UPDATED_AT, ANSWERED_AT);

        assertEquals(inquiry, other);
        assertEquals(inquiry.hashCode(), other.hashCode());
    }

    @Test
    @DisplayName("아이디가 다른 문의는 내용이 같아도 다른 문의이다")
    void notEqualsById() {
        Inquiry inquiry = Inquiry.withId(
                INQUIRY_ID, MEMBER_ID, CONTACT_EMAIL, InquiryStatus.WAITING, TITLE, CONTENT,
                CREATED_AT, UPDATED_AT, null);
        Inquiry other = Inquiry.withId(
                new InquiryId(2L), MEMBER_ID, CONTACT_EMAIL, InquiryStatus.WAITING, TITLE, CONTENT,
                CREATED_AT, UPDATED_AT, null);

        assertNotEquals(inquiry, other);
    }

    @Test
    @DisplayName("답변 대기 중 상태의 문의는 삭제할 수 있다")
    void canDeleteWhenWaiting() {
        Inquiry inquiry = Inquiry.withId(
                INQUIRY_ID, MEMBER_ID, CONTACT_EMAIL, InquiryStatus.WAITING, TITLE, CONTENT,
                CREATED_AT, UPDATED_AT, null);

        assertTrue(inquiry.canDelete());
    }

    @Test
    @DisplayName("답변 완료 상태의 문의는 삭제할 수 없다")
    void cannotDeleteWhenAnswered() {
        Inquiry inquiry = Inquiry.withId(
                INQUIRY_ID, MEMBER_ID, CONTACT_EMAIL, InquiryStatus.ANSWERED, TITLE, CONTENT,
                CREATED_AT, UPDATED_AT, ANSWERED_AT);

        assertFalse(inquiry.canDelete());
    }
}
