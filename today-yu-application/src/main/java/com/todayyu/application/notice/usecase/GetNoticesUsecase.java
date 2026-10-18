package com.todayyu.application.notice.usecase;

import com.todayyu.application.common.annotation.Usecase;
import com.todayyu.application.common.dto.CursorResult;
import com.todayyu.application.notice.dto.query.GetNoticesQuery;
import com.todayyu.application.notice.dto.result.NoticeSummaryResult;
import com.todayyu.application.notice.port.dto.NoticeCursor;
import com.todayyu.application.notice.port.dto.NoticeSummaryView;
import com.todayyu.application.notice.port.read.ReadNoticePort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Usecase
@RequiredArgsConstructor
public class GetNoticesUsecase {
    private final ReadNoticePort readNoticePort;

    /**
     * 공지 목록 조회 메서드
     *
     * <p> {@link NoticeCursor}를 통해 공지 목록을 조회하며, 이후 공지가 존재하는지 확인하기 위해
     *     {@code GetNoticesQuery.size() + 1} 개의 레코드를 조회한다. 커서가 {@code null}이면 첫 페이지를 조회한다. </p>
     *
     * <ul>
     *     <li> {@code 조회 결과 size > 요청 size} 일 경우 다음 레코드가 존재한다. </li>
     *     <li> {@code 조회 결과 size <= 요청 size} 일 경우 다음 레코드가 존재하지 않는다. </li>
     * </ul>
     *
     * <p> 다음 공지가 존재할 경우 {@code nextCursor}를 생성해 반환한다. </p>
     *
     * @param query 공지 목록 조회 요청 DTO
     * @return 공지 목록
     */
    @Transactional(readOnly = true)
    public CursorResult<NoticeSummaryResult, NoticeCursor> getNotices(GetNoticesQuery query) {
        int size = query.size();
        List<NoticeSummaryView> views = readNoticePort.readByCursor(query.cursor(), size + 1);
        
        boolean hasNext = views.size() > size;
        List<NoticeSummaryView> contents = hasNext ? views.subList(0, size) : views;
        
        List<NoticeSummaryResult> results = contents.stream()
                .map(view -> NoticeSummaryResult.from(view))
                .toList();
        
        NoticeCursor nextCursor = hasNext
                ? NoticeCursor.from(contents.get(contents.size() - 1))
                : null;
        
        return new CursorResult<>(results, hasNext, nextCursor);
    }
}
