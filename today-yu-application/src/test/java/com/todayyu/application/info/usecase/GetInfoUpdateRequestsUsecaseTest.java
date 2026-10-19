package com.todayyu.application.info.usecase;

import com.todayyu.application.common.dto.CursorResult;
import com.todayyu.application.info.dto.query.GetInfoUpdateRequestsQuery;
import com.todayyu.application.info.dto.result.InfoUpdateRequestSummaryResult;
import com.todayyu.application.info.dto.InfoUpdateRequestCursor;
import com.todayyu.application.info.dto.InfoUpdateRequestSortType;
import com.todayyu.application.info.port.dto.InfoUpdateRequestSummaryView;
import com.todayyu.application.info.port.read.ReadInfoUpdateRequestPort;
import com.todayyu.domain.info.vo.InfoCategory;
import com.todayyu.domain.info.vo.InfoRequestStatus;
import com.todayyu.domain.info.vo.InfoTitle;
import com.todayyu.domain.info.vo.InfoUpdateRequestId;
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
class GetInfoUpdateRequestsUsecaseTest {
    private static final int SIZE = 2;
    private static final MemberId REQUESTER_ID = new MemberId(1L);
    private static final InfoCategory CATEGORY = InfoCategory.ACADEMIC;
    private static final InfoUpdateRequestSortType SORT_TYPE = InfoUpdateRequestSortType.LATEST;
    private static final InfoUpdateRequestCursor CURSOR = new InfoUpdateRequestCursor(
            LocalDateTime.of(2026, 10, 10, 9, 0),
            new InfoUpdateRequestId(10L)
    );
    private static final GetInfoUpdateRequestsQuery QUERY = new GetInfoUpdateRequestsQuery(
            REQUESTER_ID,
            CATEGORY,
            SORT_TYPE,
            CURSOR,
            SIZE
    );

    @Mock
    private ReadInfoUpdateRequestPort readInfoUpdateRequestPort;

    @InjectMocks
    private GetInfoUpdateRequestsUsecase getInfoUpdateRequestsUsecase;

    @Test
    @DisplayName("요청 크기보다 1개 더 조회되면 요청 크기만큼 반환하고, 다음 데이터가 존재하며 마지막 정보 수정 요청으로 다음 커서를 생성한다")
    void getInfoUpdateRequestsWithNext() {
        // given
        InfoUpdateRequestSummaryView first = view(9L, LocalDateTime.of(2026, 10, 9, 9, 0));
        InfoUpdateRequestSummaryView second = view(8L, LocalDateTime.of(2026, 10, 8, 9, 0));
        InfoUpdateRequestSummaryView extra = view(7L, LocalDateTime.of(2026, 10, 7, 9, 0));
        given(readInfoUpdateRequestPort.readByCursor(REQUESTER_ID, CATEGORY, SORT_TYPE, CURSOR, SIZE + 1))
                .willReturn(List.of(first, second, extra));

        // when
        CursorResult<InfoUpdateRequestSummaryResult, InfoUpdateRequestCursor> result
                = getInfoUpdateRequestsUsecase.getInfoUpdateRequests(QUERY);

        // then
        List<InfoUpdateRequestSummaryResult> expected = List.of(
                InfoUpdateRequestSummaryResult.from(first),
                InfoUpdateRequestSummaryResult.from(second)
        );
        assertEquals(expected, result.contents());
        assertTrue(result.hasNext());
        assertEquals(new InfoUpdateRequestCursor(second.requestedAt(), second.id()), result.nextCursor());
    }

    @Test
    @DisplayName("요청 크기만큼 조회되면 모두 반환하고, 다음 데이터가 존재하지 않으며 다음 커서는 null이다")
    void getInfoUpdateRequestsWithoutNext() {
        // given
        InfoUpdateRequestSummaryView first = view(9L, LocalDateTime.of(2026, 10, 9, 9, 0));
        InfoUpdateRequestSummaryView second = view(8L, LocalDateTime.of(2026, 10, 8, 9, 0));
        given(readInfoUpdateRequestPort.readByCursor(REQUESTER_ID, CATEGORY, SORT_TYPE, CURSOR, SIZE + 1))
                .willReturn(List.of(first, second));

        // when
        CursorResult<InfoUpdateRequestSummaryResult, InfoUpdateRequestCursor> result
                = getInfoUpdateRequestsUsecase.getInfoUpdateRequests(QUERY);

        // then
        assertEquals(2, result.contents().size());
        assertFalse(result.hasNext());
        assertNull(result.nextCursor());
    }

    @Test
    @DisplayName("조회된 정보 수정 요청이 없으면 빈 목록을 반환하고, 다음 데이터가 존재하지 않으며 다음 커서는 null이다")
    void getInfoUpdateRequestsWithEmptyResult() {
        // given
        given(readInfoUpdateRequestPort.readByCursor(REQUESTER_ID, CATEGORY, SORT_TYPE, CURSOR, SIZE + 1))
                .willReturn(List.of());

        // when
        CursorResult<InfoUpdateRequestSummaryResult, InfoUpdateRequestCursor> result
                = getInfoUpdateRequestsUsecase.getInfoUpdateRequests(QUERY);

        // then
        assertTrue(result.contents().isEmpty());
        assertFalse(result.hasNext());
        assertNull(result.nextCursor());
    }

    @Test
    @DisplayName("첫 요청(커서·카테고리 없음)이면 요청자 ID로만 커서와 카테고리 없이 정보 수정 요청을 조회한다")
    void getInfoUpdateRequestsWithFirstRequest() {
        // given
        GetInfoUpdateRequestsQuery query = new GetInfoUpdateRequestsQuery(
                REQUESTER_ID,
                null,
                InfoUpdateRequestSortType.OLDEST,
                null,
                SIZE
        );
        given(readInfoUpdateRequestPort.readByCursor(
                REQUESTER_ID, null, InfoUpdateRequestSortType.OLDEST, null, SIZE + 1
        )).willReturn(List.of());

        // when
        getInfoUpdateRequestsUsecase.getInfoUpdateRequests(query);

        // then
        then(readInfoUpdateRequestPort).should().readByCursor(
                REQUESTER_ID, null, InfoUpdateRequestSortType.OLDEST, null, SIZE + 1
        );
    }

    private InfoUpdateRequestSummaryView view(Long id, LocalDateTime requestedAt) {
        return new InfoUpdateRequestSummaryView(
                new InfoUpdateRequestId(id),
                CATEGORY,
                new InfoTitle("정보 " + id),
                InfoRequestStatus.PENDING,
                requestedAt
        );
    }
}
