package com.todayyu.application.scrap.usecase;

import com.todayyu.application.common.dto.CursorResult;
import com.todayyu.application.scrap.dto.ScrapCursor;
import com.todayyu.application.scrap.dto.ScrapSortType;
import com.todayyu.application.scrap.dto.query.GetScrapsQuery;
import com.todayyu.application.scrap.dto.result.ScrapResult;
import com.todayyu.application.scrap.port.dto.ScrapView;
import com.todayyu.application.scrap.port.read.ReadScrapPort;
import com.todayyu.domain.info.vo.InfoCategory;
import com.todayyu.domain.info.vo.InfoId;
import com.todayyu.domain.info.vo.InfoTitle;
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
class GetScrapsUsecaseTest {
    private static final int SIZE = 2;
    private static final MemberId MEMBER_ID = new MemberId(1L);
    private static final InfoCategory CATEGORY = InfoCategory.ACADEMIC;
    private static final ScrapSortType SORT_TYPE = ScrapSortType.LATEST;
    private static final ScrapCursor CURSOR = new ScrapCursor(LocalDateTime.of(2026, 10, 10, 9, 0), new InfoId(10L));
    private static final GetScrapsQuery QUERY = new GetScrapsQuery(MEMBER_ID, CATEGORY, CURSOR, SORT_TYPE, SIZE);

    @Mock
    private ReadScrapPort readScrapPort;

    @InjectMocks
    private GetScrapsUsecase getScrapsUsecase;

    @Test
    @DisplayName("요청 크기보다 1개 더 조회되면 요청 크기만큼 반환하고, 다음 데이터가 존재하며 마지막 정보로 다음 커서를 생성한다")
    void getScrapsWithNext() {
        // given
        ScrapView first = view(9L, LocalDateTime.of(2026, 10, 9, 9, 0));
        ScrapView second = view(8L, LocalDateTime.of(2026, 10, 8, 9, 0));
        ScrapView extra = view(7L, LocalDateTime.of(2026, 10, 7, 9, 0));
        given(readScrapPort.readByCursor(MEMBER_ID, CATEGORY, SORT_TYPE, CURSOR, SIZE + 1))
                .willReturn(List.of(first, second, extra));

        // when
        CursorResult<ScrapResult, ScrapCursor> result = getScrapsUsecase.getScraps(QUERY);

        // then
        List<ScrapResult> expected = List.of(
                ScrapResult.from(first),
                ScrapResult.from(second)
        );
        assertEquals(expected, result.contents());
        assertTrue(result.hasNext());
        assertEquals(new ScrapCursor(second.updatedAt(), second.infoId()), result.nextCursor());
    }

    @Test
    @DisplayName("요청 크기만큼 조회되면 모두 반환하고, 다음 데이터가 존재하지 않으며 다음 커서는 null이다")
    void getScrapsWithoutNext() {
        // given
        ScrapView first = view(9L, LocalDateTime.of(2026, 10, 9, 9, 0));
        ScrapView second = view(8L, LocalDateTime.of(2026, 10, 8, 9, 0));
        given(readScrapPort.readByCursor(MEMBER_ID, CATEGORY, SORT_TYPE, CURSOR, SIZE + 1))
                .willReturn(List.of(first, second));

        // when
        CursorResult<ScrapResult, ScrapCursor> result = getScrapsUsecase.getScraps(QUERY);

        // then
        assertEquals(2, result.contents().size());
        assertFalse(result.hasNext());
        assertNull(result.nextCursor());
    }

    @Test
    @DisplayName("조회된 스크랩이 없으면 빈 목록을 반환하고, 다음 데이터가 존재하지 않으며 다음 커서는 null이다")
    void getScrapsWithEmptyResult() {
        // given
        given(readScrapPort.readByCursor(MEMBER_ID, CATEGORY, SORT_TYPE, CURSOR, SIZE + 1))
                .willReturn(List.of());

        // when
        CursorResult<ScrapResult, ScrapCursor> result = getScrapsUsecase.getScraps(QUERY);

        // then
        assertTrue(result.contents().isEmpty());
        assertFalse(result.hasNext());
        assertNull(result.nextCursor());
    }

    @Test
    @DisplayName("첫 요청(커서 없음)이고 카테고리가 없으면 커서와 카테고리 없이 스크랩한 정보를 조회한다")
    void getScrapsWithFirstRequestAndAllCategories() {
        // given
        GetScrapsQuery query = new GetScrapsQuery(MEMBER_ID, null, null, SORT_TYPE, SIZE);
        given(readScrapPort.readByCursor(MEMBER_ID, null, SORT_TYPE, null, SIZE + 1))
                .willReturn(List.of());

        // when
        getScrapsUsecase.getScraps(query);

        // then
        then(readScrapPort).should().readByCursor(MEMBER_ID, null, SORT_TYPE, null, SIZE + 1);
    }

    private ScrapView view(Long infoId, LocalDateTime updatedAt) {
        return new ScrapView(
                new InfoId(infoId),
                CATEGORY,
                new InfoTitle("정보 " + infoId),
                infoId,
                updatedAt
        );
    }
}
