package com.todayyu.application.info.usecase;

import com.todayyu.application.common.dto.CursorResult;
import com.todayyu.application.info.dto.query.GetInfoRegistrationRequestsQuery;
import com.todayyu.application.info.dto.result.InfoRegistrationRequestSummaryResult;
import com.todayyu.application.info.dto.InfoRegistrationRequestCursor;
import com.todayyu.application.info.dto.InfoRegistrationRequestSortType;
import com.todayyu.application.info.port.dto.InfoRegistrationRequestSummaryView;
import com.todayyu.application.info.port.read.ReadInfoRegistrationRequestPort;
import com.todayyu.domain.info.vo.InfoCategory;
import com.todayyu.domain.info.vo.InfoRegistrationRequestId;
import com.todayyu.domain.info.vo.InfoRegistrationRequestTitle;
import com.todayyu.domain.info.vo.InfoRequestStatus;
import com.todayyu.domain.member.vo.MemberId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class GetInfoRegistrationRequestsUsecaseTest {
    private static final int SIZE = 2;
    private static final MemberId REQUESTER_ID = new MemberId(1L);
    private static final InfoCategory CATEGORY = InfoCategory.ACADEMIC;
    private static final InfoRegistrationRequestSortType SORT_TYPE = InfoRegistrationRequestSortType.LATEST;
    private static final InfoRegistrationRequestCursor CURSOR = new InfoRegistrationRequestCursor(
            LocalDateTime.of(2026, 10, 10, 9, 0),
            new InfoRegistrationRequestId(10L)
    );
    private static final GetInfoRegistrationRequestsQuery QUERY = new GetInfoRegistrationRequestsQuery(
            REQUESTER_ID,
            CATEGORY,
            SORT_TYPE,
            CURSOR,
            SIZE
    );

    @Mock
    private ReadInfoRegistrationRequestPort readInfoRegistrationRequestPort;

    @InjectMocks
    private GetInfoRegistrationRequestsUsecase getInfoRegistrationRequestsUsecase;

    @Test
    @DisplayName("요청 크기보다 1개 더 조회되면 요청 크기만큼 반환하고, 다음 데이터가 존재하며 마지막 정보 등록 요청으로 다음 커서를 생성한다")
    void getInfoRegistrationRequestsWithNext() {
        // given
        InfoRegistrationRequestSummaryView first = view(9L, LocalDateTime.of(2026, 10, 9, 9, 0));
        InfoRegistrationRequestSummaryView second = view(8L, LocalDateTime.of(2026, 10, 8, 9, 0));
        InfoRegistrationRequestSummaryView extra = view(7L, LocalDateTime.of(2026, 10, 7, 9, 0));
        given(readInfoRegistrationRequestPort.readByCursor(REQUESTER_ID, CATEGORY, SORT_TYPE, CURSOR, SIZE + 1))
                .willReturn(List.of(first, second, extra));

        // when
        CursorResult<InfoRegistrationRequestSummaryResult, InfoRegistrationRequestCursor> result
                = getInfoRegistrationRequestsUsecase.getInfoRegistrationRequests(QUERY);

        // then
        List<InfoRegistrationRequestSummaryResult> expected = List.of(
                InfoRegistrationRequestSummaryResult.from(first),
                InfoRegistrationRequestSummaryResult.from(second)
        );
        assertEquals(expected, result.contents());
        assertTrue(result.hasNext());
        assertEquals(new InfoRegistrationRequestCursor(second.requestedAt(), second.id()), result.nextCursor());
    }

    @Test
    @DisplayName("요청 크기만큼 조회되면 모두 반환하고, 다음 데이터가 존재하지 않으며 다음 커서는 null이다")
    void getInfoRegistrationRequestsWithoutNext() {
        // given
        InfoRegistrationRequestSummaryView first = view(9L, LocalDateTime.of(2026, 10, 9, 9, 0));
        InfoRegistrationRequestSummaryView second = view(8L, LocalDateTime.of(2026, 10, 8, 9, 0));
        given(readInfoRegistrationRequestPort.readByCursor(REQUESTER_ID, CATEGORY, SORT_TYPE, CURSOR, SIZE + 1))
                .willReturn(List.of(first, second));

        // when
        CursorResult<InfoRegistrationRequestSummaryResult, InfoRegistrationRequestCursor> result
                = getInfoRegistrationRequestsUsecase.getInfoRegistrationRequests(QUERY);

        // then
        assertEquals(2, result.contents().size());
        assertFalse(result.hasNext());
        assertNull(result.nextCursor());
    }

    @Test
    @DisplayName("조회된 정보 등록 요청이 없으면 빈 목록을 반환하고, 다음 데이터가 존재하지 않으며 다음 커서는 null이다")
    void getInfoRegistrationRequestsWithEmptyResult() {
        // given
        given(readInfoRegistrationRequestPort.readByCursor(REQUESTER_ID, CATEGORY, SORT_TYPE, CURSOR, SIZE + 1))
                .willReturn(List.of());

        // when
        CursorResult<InfoRegistrationRequestSummaryResult, InfoRegistrationRequestCursor> result
                = getInfoRegistrationRequestsUsecase.getInfoRegistrationRequests(QUERY);

        // then
        assertTrue(result.contents().isEmpty());
        assertFalse(result.hasNext());
        assertNull(result.nextCursor());
    }

    @Test
    @DisplayName("첫 요청(커서·카테고리 없음)이면 요청자 ID로만 커서와 카테고리 없이 정보 등록 요청을 조회한다")
    void getInfoRegistrationRequestsWithFirstRequest() {
        // given
        GetInfoRegistrationRequestsQuery query = new GetInfoRegistrationRequestsQuery(
                REQUESTER_ID,
                null,
                InfoRegistrationRequestSortType.OLDEST,
                null,
                SIZE
        );
        given(readInfoRegistrationRequestPort.readByCursor(
                REQUESTER_ID, null, InfoRegistrationRequestSortType.OLDEST, null, SIZE + 1
        )).willReturn(List.of());

        // when
        getInfoRegistrationRequestsUsecase.getInfoRegistrationRequests(query);

        // then
        then(readInfoRegistrationRequestPort).should().readByCursor(
                REQUESTER_ID, null, InfoRegistrationRequestSortType.OLDEST, null, SIZE + 1
        );
    }

    private InfoRegistrationRequestSummaryView view(Long id, LocalDateTime requestedAt) {
        return InfoRegistrationRequestSummaryView.builder()
                .id(new InfoRegistrationRequestId(id))
                .category(CATEGORY)
                .title(new InfoRegistrationRequestTitle("정보 등록 요청 " + id))
                .status(InfoRequestStatus.PENDING)
                .requestedAt(requestedAt)
                .build();
    }
}
