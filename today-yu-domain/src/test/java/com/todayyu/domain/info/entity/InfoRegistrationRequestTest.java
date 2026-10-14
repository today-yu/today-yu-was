package com.todayyu.domain.info.entity;

import com.todayyu.domain.common.vo.Email;
import com.todayyu.domain.info.vo.InfoCategory;
import com.todayyu.domain.info.vo.InfoId;
import com.todayyu.domain.info.vo.InfoRegistrationRequestContent;
import com.todayyu.domain.info.vo.InfoRegistrationRequestId;
import com.todayyu.domain.info.vo.InfoRegistrationRequestTitle;
import com.todayyu.domain.info.vo.InfoRequestStatus;
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

class InfoRegistrationRequestTest {
    private static final InfoRegistrationRequestId REQUEST_ID = new InfoRegistrationRequestId(1L);
    private static final MemberId REQUESTER_ID = new MemberId(1L);
    private static final InfoRegistrationRequestTitle TITLE =
            new InfoRegistrationRequestTitle("2학기 수강신청 정정 기간 안내");
    private static final InfoRegistrationRequestContent CONTENT =
            new InfoRegistrationRequestContent("2학기 수강신청 정정 기간은 9월 첫째 주입니다.");
    private static final Email CONTACT_EMAIL = new Email("test@yu.ac.kr");
    private static final InfoId INFO_ID = new InfoId(1L);
    private static final RejectionReason REJECTION_REASON = new RejectionReason("이미 등록된 정보입니다.");
    private static final LocalDateTime REQUESTED_AT = LocalDateTime.of(2026, 10, 1, 9, 0);

    @Test
    @DisplayName("아이디가 없는 신규 정보 등록 요청을 생성할 수 있다")
    void createWithoutId() {
        // when
        InfoRegistrationRequest request = InfoRegistrationRequest.withoutId(
                REQUESTER_ID, InfoCategory.ACADEMIC, TITLE, CONTENT, CONTACT_EMAIL, REQUESTED_AT);

        // then
        assertNull(request.getId());
        assertEquals(REQUESTER_ID, request.getRequesterId());
        assertEquals(InfoCategory.ACADEMIC, request.getCategory());
        assertEquals(TITLE, request.getTitle());
        assertEquals(CONTENT, request.getContent());
        assertEquals(CONTACT_EMAIL, request.getContactEmail());
        assertEquals(REQUESTED_AT, request.getRequestedAt());
    }

    @Test
    @DisplayName("신규 정보 등록 요청은 검토 중 상태이며 관련 정보와 반려 사유가 없다")
    void pendingWhenCreated() {
        InfoRegistrationRequest request = InfoRegistrationRequest.withoutId(
                REQUESTER_ID, InfoCategory.ACADEMIC, TITLE, CONTENT, CONTACT_EMAIL, REQUESTED_AT);

        assertEquals(InfoRequestStatus.PENDING, request.getStatus());
        assertNull(request.getInfoId());
        assertNull(request.getRejectionReason());
    }

    @Test
    @DisplayName("요청 회원 없이 신규 정보 등록 요청을 생성할 수 없다")
    void createWithoutIdWithoutRequesterId() {
        assertThrows(IllegalArgumentException.class,
                () -> InfoRegistrationRequest.withoutId(null, InfoCategory.ACADEMIC, TITLE, CONTENT, CONTACT_EMAIL, REQUESTED_AT));
    }

    @Test
    @DisplayName("카테고리 없이 신규 정보 등록 요청을 생성할 수 없다")
    void createWithoutIdWithoutCategory() {
        assertThrows(IllegalArgumentException.class,
                () -> InfoRegistrationRequest.withoutId(REQUESTER_ID, null, TITLE, CONTENT, CONTACT_EMAIL, REQUESTED_AT));
    }

    @Test
    @DisplayName("요청 제목 없이 신규 정보 등록 요청을 생성할 수 없다")
    void createWithoutIdWithoutTitle() {
        assertThrows(IllegalArgumentException.class,
                () -> InfoRegistrationRequest.withoutId(REQUESTER_ID, InfoCategory.ACADEMIC, null, CONTENT, CONTACT_EMAIL, REQUESTED_AT));
    }

    @Test
    @DisplayName("요청 내용 없이 신규 정보 등록 요청을 생성할 수 없다")
    void createWithoutIdWithoutContent() {
        assertThrows(IllegalArgumentException.class,
                () -> InfoRegistrationRequest.withoutId(REQUESTER_ID, InfoCategory.ACADEMIC, TITLE, null, CONTACT_EMAIL, REQUESTED_AT));
    }

    @Test
    @DisplayName("연락용 이메일 없이 신규 정보 등록 요청을 생성할 수 없다")
    void createWithoutIdWithoutContactEmail() {
        assertThrows(IllegalArgumentException.class,
                () -> InfoRegistrationRequest.withoutId(REQUESTER_ID, InfoCategory.ACADEMIC, TITLE, CONTENT, null, REQUESTED_AT));
    }

    @Test
    @DisplayName("저장된 정보 등록 요청으로 아이디를 가진 정보 등록 요청을 생성할 수 있다")
    void createWithId() {
        // when
        InfoRegistrationRequest request = InfoRegistrationRequest.withId(
                REQUEST_ID, REQUESTER_ID, InfoCategory.ACADEMIC, TITLE, CONTENT, InfoRequestStatus.ACCEPTED,
                INFO_ID, CONTACT_EMAIL, null, REQUESTED_AT);

        // then
        assertEquals(REQUEST_ID, request.getId());
        assertEquals(REQUESTER_ID, request.getRequesterId());
        assertEquals(InfoCategory.ACADEMIC, request.getCategory());
        assertEquals(TITLE, request.getTitle());
        assertEquals(CONTENT, request.getContent());
        assertEquals(InfoRequestStatus.ACCEPTED, request.getStatus());
        assertEquals(INFO_ID, request.getInfoId());
        assertEquals(CONTACT_EMAIL, request.getContactEmail());
        assertNull(request.getRejectionReason());
        assertEquals(REQUESTED_AT, request.getRequestedAt());
    }

    @Test
    @DisplayName("아이디 없이 저장된 정보 등록 요청을 생성할 수 없다")
    void createWithIdWithoutRequestId() {
        assertThrows(IllegalArgumentException.class,
                () -> InfoRegistrationRequest.withId(
                        null, REQUESTER_ID, InfoCategory.ACADEMIC, TITLE, CONTENT, InfoRequestStatus.PENDING,
                        null, CONTACT_EMAIL, null, REQUESTED_AT));
    }

    @Test
    @DisplayName("요청 회원 없이 저장된 정보 등록 요청을 생성할 수 없다")
    void createWithIdWithoutRequesterId() {
        assertThrows(IllegalArgumentException.class,
                () -> InfoRegistrationRequest.withId(
                        REQUEST_ID, null, InfoCategory.ACADEMIC, TITLE, CONTENT, InfoRequestStatus.PENDING,
                        null, CONTACT_EMAIL, null, REQUESTED_AT));
    }

    @Test
    @DisplayName("카테고리 없이 저장된 정보 등록 요청을 생성할 수 없다")
    void createWithIdWithoutCategory() {
        assertThrows(IllegalArgumentException.class,
                () -> InfoRegistrationRequest.withId(
                        REQUEST_ID, REQUESTER_ID, null, TITLE, CONTENT, InfoRequestStatus.PENDING,
                        null, CONTACT_EMAIL, null, REQUESTED_AT));
    }

    @Test
    @DisplayName("요청 제목 없이 저장된 정보 등록 요청을 생성할 수 없다")
    void createWithIdWithoutTitle() {
        assertThrows(IllegalArgumentException.class,
                () -> InfoRegistrationRequest.withId(
                        REQUEST_ID, REQUESTER_ID, InfoCategory.ACADEMIC, null, CONTENT, InfoRequestStatus.PENDING,
                        null, CONTACT_EMAIL, null, REQUESTED_AT));
    }

    @Test
    @DisplayName("요청 내용 없이 저장된 정보 등록 요청을 생성할 수 없다")
    void createWithIdWithoutContent() {
        assertThrows(IllegalArgumentException.class,
                () -> InfoRegistrationRequest.withId(
                        REQUEST_ID, REQUESTER_ID, InfoCategory.ACADEMIC, TITLE, null, InfoRequestStatus.PENDING,
                        null, CONTACT_EMAIL, null, REQUESTED_AT));
    }

    @Test
    @DisplayName("연락용 이메일 없이 저장된 정보 등록 요청을 생성할 수 없다")
    void createWithIdWithoutContactEmail() {
        assertThrows(IllegalArgumentException.class,
                () -> InfoRegistrationRequest.withId(
                        REQUEST_ID, REQUESTER_ID, InfoCategory.ACADEMIC, TITLE, CONTENT, InfoRequestStatus.PENDING,
                        null, null, null, REQUESTED_AT));
    }

    @Test
    @DisplayName("상태 없이 저장된 정보 등록 요청을 생성할 수 없다")
    void createWithIdWithoutStatus() {
        assertThrows(IllegalArgumentException.class,
                () -> InfoRegistrationRequest.withId(
                        REQUEST_ID, REQUESTER_ID, InfoCategory.ACADEMIC, TITLE, CONTENT, null,
                        null, CONTACT_EMAIL, null, REQUESTED_AT));
    }

    static Stream<Arguments> validStatusCombinations() {
        return Stream.of(
                Arguments.of(InfoRequestStatus.PENDING, null, null),
                Arguments.of(InfoRequestStatus.ACCEPTED, INFO_ID, null),
                Arguments.of(InfoRequestStatus.REJECTED, null, REJECTION_REASON)
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("validStatusCombinations")
    @DisplayName("상태에 맞는 관련 정보와 반려 사유로 저장된 정보 등록 요청을 생성할 수 있다")
    void createWithIdWithValidStatus(InfoRequestStatus status, InfoId infoId, RejectionReason rejectionReason) {
        assertDoesNotThrow(() -> InfoRegistrationRequest.withId(
                REQUEST_ID, REQUESTER_ID, InfoCategory.ACADEMIC, TITLE, CONTENT, status,
                infoId, CONTACT_EMAIL, rejectionReason, REQUESTED_AT));
    }

    static Stream<Arguments> invalidStatusCombinations() {
        return Stream.of(
                Arguments.of(InfoRequestStatus.PENDING, INFO_ID, null),
                Arguments.of(InfoRequestStatus.PENDING, null, REJECTION_REASON),
                Arguments.of(InfoRequestStatus.PENDING, INFO_ID, REJECTION_REASON),
                Arguments.of(InfoRequestStatus.ACCEPTED, null, null),
                Arguments.of(InfoRequestStatus.ACCEPTED, null, REJECTION_REASON),
                Arguments.of(InfoRequestStatus.ACCEPTED, INFO_ID, REJECTION_REASON),
                Arguments.of(InfoRequestStatus.REJECTED, null, null),
                Arguments.of(InfoRequestStatus.REJECTED, INFO_ID, null),
                Arguments.of(InfoRequestStatus.REJECTED, INFO_ID, REJECTION_REASON)
        );
    }

    @ParameterizedTest(name = "{0} - 관련 정보: {1}, 반려 사유: {2}")
    @MethodSource("invalidStatusCombinations")
    @DisplayName("상태에 맞지 않는 관련 정보와 반려 사유로 저장된 정보 등록 요청을 생성할 수 없다")
    void createWithIdWithInvalidStatus(InfoRequestStatus status, InfoId infoId, RejectionReason rejectionReason) {
        assertThrows(IllegalArgumentException.class,
                () -> InfoRegistrationRequest.withId(
                        REQUEST_ID, REQUESTER_ID, InfoCategory.ACADEMIC, TITLE, CONTENT, status,
                        infoId, CONTACT_EMAIL, rejectionReason, REQUESTED_AT));
    }

    @Test
    @DisplayName("아이디가 같은 정보 등록 요청은 내용이 달라도 같은 정보 등록 요청이다")
    void equalsById() {
        InfoRegistrationRequest request = InfoRegistrationRequest.withId(
                REQUEST_ID, REQUESTER_ID, InfoCategory.ACADEMIC, TITLE, CONTENT, InfoRequestStatus.PENDING,
                null, CONTACT_EMAIL, null, REQUESTED_AT);
        InfoRegistrationRequest other = InfoRegistrationRequest.withId(
                REQUEST_ID, new MemberId(2L), InfoCategory.WELFARE, TITLE, CONTENT, InfoRequestStatus.REJECTED,
                null, new Email("other@yu.ac.kr"), REJECTION_REASON, REQUESTED_AT);

        assertEquals(request, other);
        assertEquals(request.hashCode(), other.hashCode());
    }

    @Test
    @DisplayName("아이디가 다른 정보 등록 요청은 내용이 같아도 다른 정보 등록 요청이다")
    void notEqualsById() {
        InfoRegistrationRequest request = InfoRegistrationRequest.withId(
                REQUEST_ID, REQUESTER_ID, InfoCategory.ACADEMIC, TITLE, CONTENT, InfoRequestStatus.PENDING,
                null, CONTACT_EMAIL, null, REQUESTED_AT);
        InfoRegistrationRequest other = InfoRegistrationRequest.withId(
                new InfoRegistrationRequestId(2L), REQUESTER_ID, InfoCategory.ACADEMIC, TITLE, CONTENT, InfoRequestStatus.PENDING,
                null, CONTACT_EMAIL, null, REQUESTED_AT);

        assertNotEquals(request, other);
    }
}
