package com.todayyu.application.info.usecase;

import com.todayyu.application.common.annotation.Usecase;
import com.todayyu.application.common.dto.CursorResult;
import com.todayyu.application.info.dto.query.GetInfoUpdateRequestsQuery;
import com.todayyu.application.info.dto.result.InfoUpdateRequestSummaryResult;
import com.todayyu.application.info.dto.InfoUpdateRequestCursor;
import com.todayyu.application.info.port.dto.InfoUpdateRequestSummaryView;
import com.todayyu.application.info.port.read.ReadInfoUpdateRequestPort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Usecase
@RequiredArgsConstructor
public class GetInfoUpdateRequestsUsecase {
    private final ReadInfoUpdateRequestPort readInfoUpdateRequestPort;

    /**
     * 정보 수정 요청 목록 조회 메서드
     *
     * <p> 요청자 본인의 정보 수정 요청만 조회한다. 각 정보 수정 요청은 별도의 제목 없이 연결된 정보의 제목으로 식별한다. </p>
     *
     * <p> {@link InfoUpdateRequestCursor}를 통해 정보 수정 요청 목록을 조회하며, 이후 정보 수정 요청이 존재하는지 확인하기 위해
     *     {@code GetInfoUpdateRequestsQuery.size() + 1} 개의 레코드를 조회한다. </p>
     *
     * <ul>
     *     <li> {@code 조회 결과 size > 요청 size} 일 경우 다음 레코드가 존재한다. </li>
     *     <li> {@code 조회 결과 size <= 요청 size} 일 경우 다음 레코드가 존재하지 않는다. </li>
     * </ul>
     *
     * <p> 다음 정보 수정 요청이 존재할 경우 {@code nextCursor}를 생성해 반환한다. </p>
     *
     * @param query 정보 수정 요청 목록 조회 요청 DTO
     * @return 정보 수정 요청 목록
     */
    @Transactional(readOnly = true)
    public CursorResult<InfoUpdateRequestSummaryResult, InfoUpdateRequestCursor> getInfoUpdateRequests(
            GetInfoUpdateRequestsQuery query
    ) {
        int size = query.size();
        List<InfoUpdateRequestSummaryView> views = readInfoUpdateRequestPort
                .readByCursor(query.requesterId(), query.category(), query.sortType(), query.cursor(), size + 1);

        boolean hasNext = views.size() > size;
        List<InfoUpdateRequestSummaryView> contents = hasNext ? views.subList(0, size) : views;

        List<InfoUpdateRequestSummaryResult> results = contents.stream()
                .map(view -> InfoUpdateRequestSummaryResult.from(view))
                .toList();

        InfoUpdateRequestCursor nextCursor = hasNext
                ? InfoUpdateRequestCursor.from(contents.get(contents.size() - 1))
                : null;

        return new CursorResult<>(results, hasNext, nextCursor);
    }
}
