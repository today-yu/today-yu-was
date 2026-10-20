package com.todayyu.application.inquiry.usecase;

import com.todayyu.application.common.annotation.Usecase;
import com.todayyu.application.inquiry.dto.query.GetInquiryDetailQuery;
import com.todayyu.application.inquiry.dto.result.InquiryDetailResult;
import com.todayyu.application.inquiry.error.BusinessInquiryErrorType;
import com.todayyu.application.inquiry.port.read.ReadInquiryPort;
import com.todayyu.common.exception.CustomException;
import com.todayyu.domain.inquiry.entity.Inquiry;
import com.todayyu.domain.inquiry.vo.InquiryId;
import com.todayyu.domain.member.vo.MemberId;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@Usecase
@RequiredArgsConstructor
public class GetInquiryDetailUsecase {
    private final ReadInquiryPort readInquiryPort;

    /**
     * 문의 상세 조회 메서드
     *
     * @param query 문의 상세 조회 요청 DTO
     * @return 문의 상세 정보
     * @throws CustomException 문의 상세 조회에 실패한 경우
     * <ul>
     *     <li>{@link BusinessInquiryErrorType#NOT_FOUND} - 문의가 존재하지 않을 경우</li>
     *     <li>{@link BusinessInquiryErrorType#FORBIDDEN} - 문의 작성자가 아닌 경우</li>
     * </ul>
     */
    @Transactional(readOnly = true)
    public InquiryDetailResult getInquiryDetail(GetInquiryDetailQuery query) {
        MemberId memberId = query.memberId();
        InquiryId inquiryId = query.inquiryId();
        
        Inquiry inquiry = readInquiryPort.readById(inquiryId)
                .orElseThrow(() -> new CustomException(BusinessInquiryErrorType.NOT_FOUND));
        
        if (!inquiry.isAuthor(memberId)) {
            throw new CustomException(BusinessInquiryErrorType.FORBIDDEN);
        }
        
        return InquiryDetailResult.from(inquiry);
    }
}
