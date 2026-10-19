package com.todayyu.application.notice.usecase;

import com.todayyu.application.common.dto.CursorResult;
import com.todayyu.application.notice.dto.query.GetNoticesQuery;
import com.todayyu.application.notice.dto.result.NoticeSummaryResult;
import com.todayyu.application.notice.dto.NoticeCursor;
import com.todayyu.application.notice.port.dto.NoticeSummaryView;
import com.todayyu.application.notice.port.read.ReadNoticePort;
import com.todayyu.domain.notice.vo.NoticeId;
import com.todayyu.domain.notice.vo.NoticeTitle;
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
class GetNoticesUsecaseTest {
    private static final int SIZE = 2;
    private static final NoticeCursor CURSOR = new NoticeCursor(new NoticeId(10L));
    private static final GetNoticesQuery QUERY = new GetNoticesQuery(CURSOR, SIZE);

    @Mock
    private ReadNoticePort readNoticePort;

    @InjectMocks
    private GetNoticesUsecase getNoticesUsecase;

    @Test
    @DisplayName("요청 크기보다 1개 더 조회되면 요청 크기만큼 반환하고, 다음 데이터가 존재하며 마지막 공지로 다음 커서를 생성한다")
    void getNoticesWithNext() {
        // given
        NoticeSummaryView first = view(9L, LocalDateTime.of(2026, 10, 9, 9, 0));
        NoticeSummaryView second = view(8L, LocalDateTime.of(2026, 10, 8, 9, 0));
        NoticeSummaryView extra = view(7L, LocalDateTime.of(2026, 10, 7, 9, 0));
        given(readNoticePort.readByCursor(CURSOR, SIZE + 1)).willReturn(List.of(first, second, extra));

        // when
        CursorResult<NoticeSummaryResult, NoticeCursor> result = getNoticesUsecase.getNotices(QUERY);

        // then
        List<NoticeSummaryResult> expected = List.of(
                NoticeSummaryResult.from(first),
                NoticeSummaryResult.from(second)
        );
        assertEquals(expected, result.contents());
        assertTrue(result.hasNext());
        assertEquals(new NoticeCursor(second.id()), result.nextCursor());
    }

    @Test
    @DisplayName("요청 크기만큼 조회되면 모두 반환하고, 다음 데이터가 존재하지 않으며 다음 커서는 null이다")
    void getNoticesWithoutNext() {
        // given
        NoticeSummaryView first = view(9L, LocalDateTime.of(2026, 10, 9, 9, 0));
        NoticeSummaryView second = view(8L, LocalDateTime.of(2026, 10, 8, 9, 0));
        given(readNoticePort.readByCursor(CURSOR, SIZE + 1)).willReturn(List.of(first, second));

        // when
        CursorResult<NoticeSummaryResult, NoticeCursor> result = getNoticesUsecase.getNotices(QUERY);

        // then
        assertEquals(2, result.contents().size());
        assertFalse(result.hasNext());
        assertNull(result.nextCursor());
    }

    @Test
    @DisplayName("조회된 공지가 없으면 빈 목록을 반환하고, 다음 데이터가 존재하지 않으며 다음 커서는 null이다")
    void getNoticesWithEmptyResult() {
        // given
        given(readNoticePort.readByCursor(CURSOR, SIZE + 1)).willReturn(List.of());

        // when
        CursorResult<NoticeSummaryResult, NoticeCursor> result = getNoticesUsecase.getNotices(QUERY);

        // then
        assertTrue(result.contents().isEmpty());
        assertFalse(result.hasNext());
        assertNull(result.nextCursor());
    }

    @Test
    @DisplayName("첫 요청(커서 없음)이면 커서 없이 공지를 조회한다")
    void getNoticesWithFirstRequest() {
        // given
        GetNoticesQuery query = new GetNoticesQuery(null, SIZE);
        given(readNoticePort.readByCursor(null, SIZE + 1)).willReturn(List.of());

        // when
        getNoticesUsecase.getNotices(query);

        // then
        then(readNoticePort).should().readByCursor(null, SIZE + 1);
    }

    private NoticeSummaryView view(Long id, LocalDateTime publishedAt) {
        return new NoticeSummaryView(
                new NoticeId(id),
                new NoticeTitle("공지 " + id),
                publishedAt
        );
    }
}
