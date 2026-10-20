package com.todayyu.application.inquiry.usecase;

import com.todayyu.application.common.annotation.Usecase;
import com.todayyu.application.common.dto.CursorResult;
import com.todayyu.application.inquiry.dto.InquiryCursor;
import com.todayyu.application.inquiry.dto.query.GetInquiriesQuery;
import com.todayyu.application.inquiry.dto.result.InquirySummaryResult;
import com.todayyu.application.inquiry.port.dto.InquirySummaryView;
import com.todayyu.application.inquiry.port.read.ReadInquiryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Usecase
@RequiredArgsConstructor
public class GetInquiriesUsecase {
    private final ReadInquiryPort readInquiryPort;
    
    /**
     * 문의 목록 조회 메서드
     *
     * <p> {@link InquiryCursor}를 통해 요청 회원이 작성한 문의 목록을 조회하며, 이후 문의가 존재하는지 확인하기 위해
     *     {@code GetInquiriesQuery.size() + 1} 개의 레코드를 조회한다. 커서가 {@code null}이면 첫 페이지를 조회한다. </p>
     *
     * <ul>
     *     <li> {@code 조회 결과 size > 요청 size} 일 경우 다음 레코드가 존재한다. </li>
     *     <li> {@code 조회 결과 size <= 요청 size} 일 경우 다음 레코드가 존재하지 않는다. </li>
     * </ul>
     *
     * <p> 다음 문의가 존재할 경우 {@code nextCursor}를 생성해 반환한다. </p>
     *
     * @param query 문의 목록 조회 요청 DTO
     * @return 문의 목록
     */
    @Transactional(readOnly = true)
    public CursorResult<InquirySummaryResult, InquiryCursor> getInquiries(GetInquiriesQuery query) {
        int size = query.size();
        
        List<InquirySummaryView> views
                = readInquiryPort.readByCursor(query.memberId(), query.cursor(), size + 1);
        
        boolean hasNext = views.size() > size;
        List<InquirySummaryView> contents = hasNext ? views.subList(0, size) : views;
        
        List<InquirySummaryResult> results = contents.stream()
                .map(view -> InquirySummaryResult.from(view))
                .toList();
        
        InquiryCursor nextCursor = hasNext
                ? nextCursor(contents.get(contents.size() - 1))
                : null;
        
        return new CursorResult<>(results, hasNext, nextCursor);
    }
    
    private InquiryCursor nextCursor(InquirySummaryView lastView) {
        return new InquiryCursor(lastView.id());
    }
}
