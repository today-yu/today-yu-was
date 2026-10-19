package com.todayyu.application.scrap.usecase;

import com.todayyu.application.common.annotation.Usecase;
import com.todayyu.application.common.dto.CursorResult;
import com.todayyu.application.scrap.dto.query.GetScrapsQuery;
import com.todayyu.application.scrap.dto.result.ScrapResult;
import com.todayyu.application.scrap.dto.ScrapCursor;
import com.todayyu.application.scrap.port.dto.ScrapView;
import com.todayyu.application.scrap.port.read.ReadScrapPort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Usecase
@RequiredArgsConstructor
public class GetScrapsUsecase {
    private final ReadScrapPort readScrapPort;

    /**
     * 스크랩 목록 조회 메서드
     *
     * <p> {@link ScrapCursor}를 통해 회원이 스크랩한 정보 목록을 조회하며, 이후 정보가 존재하는지 확인하기 위해
     *     {@code GetScrapsQuery.size() + 1} 개의 레코드를 조회한다. 커서가 {@code null}이면 첫 페이지를 조회한다. </p>
     *
     * <ul>
     *     <li> {@code 조회 결과 size > 요청 size} 일 경우 다음 레코드가 존재한다. </li>
     *     <li> {@code 조회 결과 size <= 요청 size} 일 경우 다음 레코드가 존재하지 않는다. </li>
     * </ul>
     *
     * <p> 다음 정보가 존재할 경우 마지막 정보의 최종 수정 시각과 정보 ID로 {@code nextCursor}를 생성해 반환한다. </p>
     *
     * @param query 스크랩 목록 조회 요청 DTO
     * @return 스크랩한 정보 목록
     */
    @Transactional(readOnly = true)
    public CursorResult<ScrapResult, ScrapCursor> getScraps(GetScrapsQuery query) {
        int size = query.size();
        
        List<ScrapView> views = readScrapPort.readByCursor(
                query.memberId(), query.category(), query.sortType(), query.cursor(), size + 1
        );
        
        boolean hasNext = views.size() > size;
        List<ScrapView> contents = hasNext ? views.subList(0, size) : views;
        
        List<ScrapResult> results = contents.stream()
                .map(ScrapResult::from)
                .toList();
        
        ScrapCursor nextCursor = hasNext
                ? nextCursor(contents.get(contents.size() - 1))
                : null;
        
        return new CursorResult<>(results, hasNext, nextCursor);
    }
    
    private ScrapCursor nextCursor(ScrapView lastView) {
        return new ScrapCursor(lastView.updatedAt(), lastView.infoId());
    }
}
