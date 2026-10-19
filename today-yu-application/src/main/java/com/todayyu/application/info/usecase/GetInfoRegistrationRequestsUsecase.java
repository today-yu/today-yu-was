package com.todayyu.application.info.usecase;

import com.todayyu.application.common.annotation.Usecase;
import com.todayyu.application.common.dto.CursorResult;
import com.todayyu.application.info.dto.query.GetInfoRegistrationRequestsQuery;
import com.todayyu.application.info.dto.result.InfoRegistrationRequestSummaryResult;
import com.todayyu.application.info.dto.InfoRegistrationRequestCursor;
import com.todayyu.application.info.port.dto.InfoRegistrationRequestSummaryView;
import com.todayyu.application.info.port.read.ReadInfoRegistrationRequestPort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Usecase
@RequiredArgsConstructor
public class GetInfoRegistrationRequestsUsecase {
    private final ReadInfoRegistrationRequestPort readInfoRegistrationRequestPort;
    
    /**
     * 정보 등록 요청 목록 조회 메서드
     *
     * <p> 요청자 본인의 정보 등록 요청만 조회한다. </p>
     *
     * <p> {@link InfoRegistrationRequestCursor}를 통해 정보 등록 요청 목록을 조회하며, 이후 정보 등록 요청이 존재하는지 확인하기 위해
     *     {@code GetInfoRegistrationRequestsQuery.size() + 1} 개의 레코드를 조회한다. </p>
     *
     * <ul>
     *     <li> {@code 조회 결과 size > 요청 size} 일 경우 다음 레코드가 존재한다. </li>
     *     <li> {@code 조회 결과 size <= 요청 size} 일 경우 다음 레코드가 존재하지 않는다. </li>
     * </ul>
     *
     * <p> 다음 정보 등록 요청이 존재할 경우 {@code nextCursor}를 생성해 반환한다. </p>
     *
     * @param query 정보 등록 요청 목록 조회 요청 DTO
     * @return 정보 등록 요청 목록
     */
    @Transactional(readOnly = true)
    public CursorResult<InfoRegistrationRequestSummaryResult, InfoRegistrationRequestCursor> getInfoRegistrationRequests(
            GetInfoRegistrationRequestsQuery query
    ) {
        int size = query.size();
        List<InfoRegistrationRequestSummaryView> views = readInfoRegistrationRequestPort
                .readByCursor(query.requesterId(), query.category(), query.sortType(), query.cursor(), size + 1);
        
        boolean hasNext = views.size() > size;
        List<InfoRegistrationRequestSummaryView> contents = hasNext ? views.subList(0, size) : views;
        
        List<InfoRegistrationRequestSummaryResult> results = contents.stream()
                .map(view -> InfoRegistrationRequestSummaryResult.from(view))
                .toList();
        
        InfoRegistrationRequestCursor nextCursor = hasNext
                ? nextCursor(contents.get(contents.size() - 1))
                : null;
        
        return new CursorResult<>(results, hasNext, nextCursor);
    }
    
    private InfoRegistrationRequestCursor nextCursor(InfoRegistrationRequestSummaryView lastView) {
        return new InfoRegistrationRequestCursor(lastView.requestedAt(), lastView.id());
    }
}
