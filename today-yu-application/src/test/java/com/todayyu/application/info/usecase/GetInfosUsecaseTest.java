package com.todayyu.application.info.usecase;

import com.todayyu.application.common.dto.CursorResult;
import com.todayyu.application.info.dto.GetInfosQuery;
import com.todayyu.application.info.dto.InfoCursor;
import com.todayyu.application.info.dto.InfoResult;
import com.todayyu.application.info.dto.InfoSortType;
import com.todayyu.application.info.dto.InfoSummaryView;
import com.todayyu.application.info.port.read.ReadHelpfulMarkPort;
import com.todayyu.application.info.port.read.ReadInfoPort;
import com.todayyu.domain.info.vo.InfoCategory;
import com.todayyu.domain.info.vo.InfoId;
import com.todayyu.domain.info.vo.InfoTitle;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class GetInfosUsecaseTest {
    private static final int SIZE = 2;
    private static final InfoCategory CATEGORY = InfoCategory.ACADEMIC;
    private static final InfoSortType SORT_TYPE = InfoSortType.LATEST;
    private static final InfoCursor CURSOR = new InfoCursor(LocalDateTime.of(2026, 10, 10, 9, 0), new InfoId(10L));
    private static final GetInfosQuery QUERY = new GetInfosQuery(CATEGORY, SORT_TYPE, CURSOR, SIZE);

    @Mock
    private ReadInfoPort readInfoPort;

    @Mock
    private ReadHelpfulMarkPort readHelpfulMarkPort;

    @InjectMocks
    private GetInfosUsecase getInfosUsecase;

    @Test
    @DisplayName("요청 크기보다 1개 더 조회되면 요청 크기만큼 반환하고, 다음 데이터가 존재하며 마지막 정보로 다음 커서를 생성한다")
    void getInfosWithNext() {
        // given
        InfoSummaryView first = view(9L, LocalDateTime.of(2026, 10, 9, 9, 0));
        InfoSummaryView second = view(8L, LocalDateTime.of(2026, 10, 8, 9, 0));
        InfoSummaryView extra = view(7L, LocalDateTime.of(2026, 10, 7, 9, 0));
        given(readInfoPort.readByCursor(CATEGORY, SORT_TYPE, CURSOR, SIZE + 1))
                .willReturn(List.of(first, second, extra));
        given(readHelpfulMarkPort.countByInfoIds(List.of(first.infoId(), second.infoId())))
                .willReturn(Map.of(first.infoId(), 3L, second.infoId(), 1L));

        // when
        CursorResult<InfoResult, InfoCursor> result = getInfosUsecase.getInfos(QUERY);

        // then
        List<InfoResult> expected = List.of(
                InfoResult.of(first, 3L),
                InfoResult.of(second, 1L)
        );
        assertEquals(expected, result.contents());
        assertTrue(result.hasNext());
        assertEquals(new InfoCursor(second.updatedAt(), second.infoId()), result.nextCursor());
    }

    @Test
    @DisplayName("요청 크기만큼 조회되면 모두 반환하고, 다음 데이터가 존재하지 않으며 다음 커서는 null이다")
    void getInfosWithoutNext() {
        // given
        InfoSummaryView first = view(9L, LocalDateTime.of(2026, 10, 9, 9, 0));
        InfoSummaryView second = view(8L, LocalDateTime.of(2026, 10, 8, 9, 0));
        given(readInfoPort.readByCursor(CATEGORY, SORT_TYPE, CURSOR, SIZE + 1))
                .willReturn(List.of(first, second));
        given(readHelpfulMarkPort.countByInfoIds(List.of(first.infoId(), second.infoId())))
                .willReturn(Map.of(first.infoId(), 3L, second.infoId(), 1L));

        // when
        CursorResult<InfoResult, InfoCursor> result = getInfosUsecase.getInfos(QUERY);

        // then
        assertEquals(2, result.contents().size());
        assertFalse(result.hasNext());
        assertNull(result.nextCursor());
    }

    @Test
    @DisplayName("'도움이 되는 정보에요' 집계 결과에 없는 정보는 '도움이 되는 정보에요' 수를 0으로 반환한다")
    void getInfosWithoutHelpfulMark() {
        // given
        InfoSummaryView marked = view(9L, LocalDateTime.of(2026, 10, 9, 9, 0));
        InfoSummaryView notMarked = view(8L, LocalDateTime.of(2026, 10, 8, 9, 0));
        given(readInfoPort.readByCursor(CATEGORY, SORT_TYPE, CURSOR, SIZE + 1))
                .willReturn(List.of(marked, notMarked));
        given(readHelpfulMarkPort.countByInfoIds(List.of(marked.infoId(), notMarked.infoId())))
                .willReturn(Map.of(marked.infoId(), 3L));

        // when
        CursorResult<InfoResult, InfoCursor> result = getInfosUsecase.getInfos(QUERY);

        // then
        assertEquals(3L, result.contents().get(0).helpfulMarkCount());
        assertEquals(0L, result.contents().get(1).helpfulMarkCount());
    }

    @Test
    @DisplayName("조회된 정보가 없으면 빈 목록을 반환하고 '도움이 되는 정보에요' 수를 조회하지 않는다")
    void getInfosWithEmptyResult() {
        // given
        given(readInfoPort.readByCursor(CATEGORY, SORT_TYPE, CURSOR, SIZE + 1)).willReturn(List.of());

        // when
        CursorResult<InfoResult, InfoCursor> result = getInfosUsecase.getInfos(QUERY);

        // then
        assertTrue(result.contents().isEmpty());
        assertFalse(result.hasNext());
        assertNull(result.nextCursor());
        then(readHelpfulMarkPort).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("첫 요청(커서·카테고리 없음)이면 커서와 카테고리 없이 정보를 조회한다")
    void getInfosWithFirstRequest() {
        // given
        GetInfosQuery query = new GetInfosQuery(null, InfoSortType.OLDEST, null, SIZE);
        given(readInfoPort.readByCursor(null, InfoSortType.OLDEST, null, SIZE + 1)).willReturn(List.of());

        // when
        getInfosUsecase.getInfos(query);

        // then
        then(readInfoPort).should().readByCursor(null, InfoSortType.OLDEST, null, SIZE + 1);
    }

    private InfoSummaryView view(Long infoId, LocalDateTime updatedAt) {
        return InfoSummaryView.builder()
                .infoId(new InfoId(infoId))
                .category(CATEGORY)
                .title(new InfoTitle("정보 " + infoId))
                .updatedAt(updatedAt)
                .build();
    }
}
