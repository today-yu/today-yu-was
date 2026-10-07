package com.todayyu.domain.info.entity;

import com.todayyu.domain.common.vo.Email;
import com.todayyu.domain.info.vo.InfoId;
import com.todayyu.domain.info.vo.InfoRequestStatus;
import com.todayyu.domain.info.vo.InfoUpdateRequestContent;
import com.todayyu.domain.info.vo.InfoUpdateRequestId;
import com.todayyu.domain.info.vo.RejectionReason;
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
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class InfoUpdateRequestTest {
    private static final InfoUpdateRequestId REQUEST_ID = new InfoUpdateRequestId(1L);
    private static final MemberId REQUESTER_ID = new MemberId(1L);
    private static final InfoId INFO_ID = new InfoId(1L);
    private static final InfoUpdateRequestContent CONTENT =
            new InfoUpdateRequestContent("중앙도서관 운영 시간이 22시까지로 변경되었습니다.");
    private static final Email CONTACT_EMAIL = new Email("test@yu.ac.kr");
    private static final RejectionReason REJECTION_REASON = new RejectionReason("변경 사실을 확인할 수 없습니다.");
    private static final LocalDateTime OBSERVED_AT = LocalDateTime.of(2026, 9, 30, 18, 0);
    private static final LocalDateTime REQUESTED_AT = LocalDateTime.of(2026, 10, 1, 9, 0);

    @Test
    @DisplayName("아이디가 없는 신규 정보 수정 요청을 생성할 수 있다")
    void createWithoutId() {
        // when
        InfoUpdateRequest request = InfoUpdateRequest.withoutId(
                REQUESTER_ID, INFO_ID, CONTENT, CONTACT_EMAIL, OBSERVED_AT, REQUESTED_AT);

        // then
        assertNull(request.getId());
        assertEquals(REQUESTER_ID, request.getRequesterId());
        assertEquals(INFO_ID, request.getInfoId());
        assertEquals(CONTENT, request.getContent());
        assertEquals(CONTACT_EMAIL, request.getContactEmail());
        assertEquals(OBSERVED_AT, request.getObservedAt());
        assertEquals(REQUESTED_AT, request.getRequestedAt());
    }

    @Test
    @DisplayName("신규 정보 수정 요청은 검토 중 상태이며 반려 사유가 없다")
    void pendingWhenCreated() {
        InfoUpdateRequest request = InfoUpdateRequest.withoutId(
                REQUESTER_ID, INFO_ID, CONTENT, CONTACT_EMAIL, OBSERVED_AT, REQUESTED_AT);

        assertEquals(InfoRequestStatus.PENDING, request.getStatus());
        assertNull(request.getRejectionReason());
    }

    @Test
    @DisplayName("요청 회원 없이 신규 정보 수정 요청을 생성할 수 없다")
    void createWithoutIdWithoutRequesterId() {
        assertThrows(IllegalArgumentException.class,
                () -> InfoUpdateRequest.withoutId(null, INFO_ID, CONTENT, CONTACT_EMAIL, OBSERVED_AT, REQUESTED_AT));
    }

    @Test
    @DisplayName("대상 정보 없이 신규 정보 수정 요청을 생성할 수 없다")
    void createWithoutIdWithoutInfoId() {
        assertThrows(IllegalArgumentException.class,
                () -> InfoUpdateRequest.withoutId(REQUESTER_ID, null, CONTENT, CONTACT_EMAIL, OBSERVED_AT, REQUESTED_AT));
    }

    @Test
    @DisplayName("요청 내용 없이 신규 정보 수정 요청을 생성할 수 없다")
    void createWithoutIdWithoutContent() {
        assertThrows(IllegalArgumentException.class,
                () -> InfoUpdateRequest.withoutId(REQUESTER_ID, INFO_ID, null, CONTACT_EMAIL, OBSERVED_AT, REQUESTED_AT));
    }

    @Test
    @DisplayName("연락용 이메일 없이 신규 정보 수정 요청을 생성할 수 없다")
    void createWithoutIdWithoutContactEmail() {
        assertThrows(IllegalArgumentException.class,
                () -> InfoUpdateRequest.withoutId(REQUESTER_ID, INFO_ID, CONTENT, null, OBSERVED_AT, REQUESTED_AT));
    }

    @Test
    @DisplayName("저장된 정보 수정 요청으로 아이디를 가진 정보 수정 요청을 생성할 수 있다")
    void createWithId() {
        // when
        InfoUpdateRequest request = InfoUpdateRequest.withId(
                REQUEST_ID, REQUESTER_ID, InfoRequestStatus.REJECTED, INFO_ID, CONTENT,
                CONTACT_EMAIL, REJECTION_REASON, OBSERVED_AT, REQUESTED_AT);

        // then
        assertEquals(REQUEST_ID, request.getId());
        assertEquals(REQUESTER_ID, request.getRequesterId());
        assertEquals(InfoRequestStatus.REJECTED, request.getStatus());
        assertEquals(INFO_ID, request.getInfoId());
        assertEquals(CONTENT, request.getContent());
        assertEquals(CONTACT_EMAIL, request.getContactEmail());
        assertEquals(REJECTION_REASON, request.getRejectionReason());
        assertEquals(OBSERVED_AT, request.getObservedAt());
        assertEquals(REQUESTED_AT, request.getRequestedAt());
    }

    @Test
    @DisplayName("아이디 없이 저장된 정보 수정 요청을 생성할 수 없다")
    void createWithIdWithoutRequestId() {
        assertThrows(IllegalArgumentException.class,
                () -> InfoUpdateRequest.withId(
                        null, REQUESTER_ID, InfoRequestStatus.PENDING, INFO_ID, CONTENT,
                        CONTACT_EMAIL, null, OBSERVED_AT, REQUESTED_AT));
    }

    @Test
    @DisplayName("요청 회원 없이 저장된 정보 수정 요청을 생성할 수 없다")
    void createWithIdWithoutRequesterId() {
        assertThrows(IllegalArgumentException.class,
                () -> InfoUpdateRequest.withId(
                        REQUEST_ID, null, InfoRequestStatus.PENDING, INFO_ID, CONTENT,
                        CONTACT_EMAIL, null, OBSERVED_AT, REQUESTED_AT));
    }

    @Test
    @DisplayName("대상 정보 없이 저장된 정보 수정 요청을 생성할 수 없다")
    void createWithIdWithoutInfoId() {
        assertThrows(IllegalArgumentException.class,
                () -> InfoUpdateRequest.withId(
                        REQUEST_ID, REQUESTER_ID, InfoRequestStatus.PENDING, null, CONTENT,
                        CONTACT_EMAIL, null, OBSERVED_AT, REQUESTED_AT));
    }

    @Test
    @DisplayName("요청 내용 없이 저장된 정보 수정 요청을 생성할 수 없다")
    void createWithIdWithoutContent() {
        assertThrows(IllegalArgumentException.class,
                () -> InfoUpdateRequest.withId(
                        REQUEST_ID, REQUESTER_ID, InfoRequestStatus.PENDING, INFO_ID, null,
                        CONTACT_EMAIL, null, OBSERVED_AT, REQUESTED_AT));
    }

    @Test
    @DisplayName("연락용 이메일 없이 저장된 정보 수정 요청을 생성할 수 없다")
    void createWithIdWithoutContactEmail() {
        assertThrows(IllegalArgumentException.class,
                () -> InfoUpdateRequest.withId(
                        REQUEST_ID, REQUESTER_ID, InfoRequestStatus.PENDING, INFO_ID, CONTENT,
                        null, null, OBSERVED_AT, REQUESTED_AT));
    }

    @Test
    @DisplayName("상태 없이 저장된 정보 수정 요청을 생성할 수 없다")
    void createWithIdWithoutStatus() {
        assertThrows(IllegalArgumentException.class,
                () -> InfoUpdateRequest.withId(
                        REQUEST_ID, REQUESTER_ID, null, INFO_ID, CONTENT,
                        CONTACT_EMAIL, null, OBSERVED_AT, REQUESTED_AT));
    }

    static Stream<Arguments> validStatusCombinations() {
        return Stream.of(
                Arguments.of(InfoRequestStatus.PENDING, null),
                Arguments.of(InfoRequestStatus.ACCEPTED, null),
                Arguments.of(InfoRequestStatus.REJECTED, REJECTION_REASON)
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("validStatusCombinations")
    @DisplayName("상태에 맞는 반려 사유로 저장된 정보 수정 요청을 생성할 수 있다")
    void createWithIdWithValidStatus(InfoRequestStatus status, RejectionReason rejectionReason) {
        assertDoesNotThrow(() -> InfoUpdateRequest.withId(
                REQUEST_ID, REQUESTER_ID, status, INFO_ID, CONTENT,
                CONTACT_EMAIL, rejectionReason, OBSERVED_AT, REQUESTED_AT));
    }

    static Stream<Arguments> invalidStatusCombinations() {
        return Stream.of(
                Arguments.of(InfoRequestStatus.PENDING, REJECTION_REASON),
                Arguments.of(InfoRequestStatus.ACCEPTED, REJECTION_REASON),
                Arguments.of(InfoRequestStatus.REJECTED, null)
        );
    }

    @ParameterizedTest(name = "{0} - 반려 사유: {1}")
    @MethodSource("invalidStatusCombinations")
    @DisplayName("상태에 맞지 않는 반려 사유로 저장된 정보 수정 요청을 생성할 수 없다")
    void createWithIdWithInvalidStatus(InfoRequestStatus status, RejectionReason rejectionReason) {
        assertThrows(IllegalArgumentException.class,
                () -> InfoUpdateRequest.withId(
                        REQUEST_ID, REQUESTER_ID, status, INFO_ID, CONTENT,
                        CONTACT_EMAIL, rejectionReason, OBSERVED_AT, REQUESTED_AT));
    }

    @Test
    @DisplayName("아이디가 같은 정보 수정 요청은 내용이 달라도 같은 정보 수정 요청이다")
    void equalsById() {
        InfoUpdateRequest request = InfoUpdateRequest.withId(
                REQUEST_ID, REQUESTER_ID, InfoRequestStatus.PENDING, INFO_ID, CONTENT,
                CONTACT_EMAIL, null, OBSERVED_AT, REQUESTED_AT);
        InfoUpdateRequest other = InfoUpdateRequest.withId(
                new InfoUpdateRequestId(1L), new MemberId(2L), InfoRequestStatus.REJECTED, new InfoId(2L), CONTENT,
                new Email("other@yu.ac.kr"), REJECTION_REASON, OBSERVED_AT, REQUESTED_AT);

        assertEquals(request, other);
        assertEquals(request.hashCode(), other.hashCode());
    }

    @Test
    @DisplayName("아이디가 다른 정보 수정 요청은 내용이 같아도 다른 정보 수정 요청이다")
    void notEqualsById() {
        InfoUpdateRequest request = InfoUpdateRequest.withId(
                REQUEST_ID, REQUESTER_ID, InfoRequestStatus.PENDING, INFO_ID, CONTENT,
                CONTACT_EMAIL, null, OBSERVED_AT, REQUESTED_AT);
        InfoUpdateRequest other = InfoUpdateRequest.withId(
                new InfoUpdateRequestId(2L), REQUESTER_ID, InfoRequestStatus.PENDING, INFO_ID, CONTENT,
                CONTACT_EMAIL, null, OBSERVED_AT, REQUESTED_AT);

        assertNotEquals(request, other);
    }
}
