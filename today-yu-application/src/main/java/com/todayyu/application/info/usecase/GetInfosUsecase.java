package com.todayyu.application.info.usecase;

import com.todayyu.application.common.annotation.Usecase;
import com.todayyu.application.common.dto.CursorResult;
import com.todayyu.application.info.dto.query.GetInfosQuery;
import com.todayyu.application.info.dto.InfoCursor;
import com.todayyu.application.info.dto.result.InfoSummaryResult;
import com.todayyu.application.info.port.dto.InfoSummaryView;
import com.todayyu.application.info.port.read.ReadHelpfulMarkPort;
import com.todayyu.application.info.port.read.ReadInfoPort;
import com.todayyu.application.scrap.port.read.ReadScrapPort;
import com.todayyu.domain.info.vo.InfoId;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Usecase
@RequiredArgsConstructor
public class GetInfosUsecase {
    private final ReadInfoPort readInfoPort;
    private final ReadHelpfulMarkPort readHelpfulMarkPort;
    private final ReadScrapPort readScrapPort;
    
    /**
     * 정보 목록 조회 메서드
     *
     * <p> {@link InfoCursor}를 통해 정보 조회를 조회하며, 이후 정보가 존재하는지 확인하기 위해
     *     {@code GetInfosQuery.size() + 1} 개의 레코드를 조회한다. </p>
     *
     * <ul>
     *     <li> {@code 조회 결과 size > 요청 size} 일 경우 다음 레코드가 존재한다. </li>
     *     <li> {@code 조회 결과 size <= 요청 size} 일 경우 다음 레코드가 존재하지 않는다. </li>
     * </ul>
     *
     * <p> 각 정보의 '도움이 되는 정보에요' 수를 조회한다. </p>
     *
     * <p> 회원의 각 정보에 대한 스크랩 여부도 조회하여 반환한다. </p>
     *
     * <p> 다음 정보가 존재할 경우 {@code nextCursor}를 생성해 반환한다. </p>
     *
     * @param query 정보 목록 조회 요청 DTO
     * @return 정보 목록
     */
    @Transactional(readOnly = true)
    public CursorResult<InfoSummaryResult, InfoCursor> getInfos(GetInfosQuery query) {
        int size = query.size();
        List<InfoSummaryView> views = readInfoPort.readByCursor(
                query.category(),
                query.sortType(),
                query.cursor(),
                size + 1
        );
        
        boolean hasNext = views.size() > size;
        List<InfoSummaryView> contents = hasNext ? views.subList(0, size) : views;
        List<InfoId> infoIds = contents.stream().map(InfoSummaryView::infoId).toList();
        
        Map<InfoId, Long> helpfulMarkCounts = infoIds.isEmpty()
                ? Map.of()
                : readHelpfulMarkPort.countByInfoIds(infoIds);
        
        Set<InfoId> scrappedInfoIds = infoIds.isEmpty()
                ? Set.of()
                : readScrapPort.readScrappedInfoIds(query.memberId(), infoIds);
        
        List<InfoSummaryResult> results = contents.stream()
                .map(view -> InfoSummaryResult.of(
                        view,
                        helpfulMarkCounts.getOrDefault(view.infoId(), 0L),
                        scrappedInfoIds.contains(view.infoId())
                ))
                .toList();
        
        InfoCursor nextCursor = hasNext
                ? nextCursor(contents.get(contents.size() - 1))
                : null;
        
        return new CursorResult<>(results, hasNext, nextCursor);
    }

    private InfoCursor nextCursor(InfoSummaryView lastView) {
        return new InfoCursor(lastView.updatedAt(), lastView.infoId());
    }
}
