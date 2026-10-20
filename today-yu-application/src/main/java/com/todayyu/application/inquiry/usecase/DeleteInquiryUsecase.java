package com.todayyu.application.inquiry.usecase;

import com.todayyu.application.common.annotation.Usecase;
import com.todayyu.application.inquiry.dto.command.DeleteInquiryCommand;
import com.todayyu.application.inquiry.error.BusinessInquiryErrorType;
import com.todayyu.application.inquiry.port.read.ReadInquiryPort;
import com.todayyu.application.inquiry.port.save.SaveInquiryPort;
import com.todayyu.common.exception.CustomException;
import com.todayyu.domain.inquiry.entity.Inquiry;
import com.todayyu.domain.inquiry.vo.InquiryId;
import com.todayyu.domain.member.vo.MemberId;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@Usecase
@RequiredArgsConstructor
public class DeleteInquiryUsecase {
    private final ReadInquiryPort readInquiryPort;
    private final SaveInquiryPort saveInquiryPort;
    
    /**
     * 문의 삭제 메서드
     *
     * @param command 문의 삭제 요청 DTO
     * @throws CustomException 문의 삭제에 실패한 경우
     * <ul>
     *     <li>{@link BusinessInquiryErrorType#NOT_FOUND} - 문의가 존재하지 않을 경우</li>
     *     <li>{@link BusinessInquiryErrorType#FORBIDDEN} - 문의 작성자가 아닌 경우</li>
     *     <li>{@link BusinessInquiryErrorType#FAIL_DELETE} - 답변 대기 중 상태의 문의가 아닌 경우</li>
     * </ul>
     */
    @Transactional
    public void delete(DeleteInquiryCommand command) {
        InquiryId id = command.id();
        MemberId memberId = command.memberId();
        
        Inquiry inquiry = readInquiryPort.readById(id)
                .orElseThrow(() -> new CustomException(BusinessInquiryErrorType.NOT_FOUND));
        
        if (!inquiry.isAuthor(memberId)) {
            throw new CustomException(BusinessInquiryErrorType.FORBIDDEN);
        }
        
        if (!inquiry.canDelete()) {
            throw new CustomException(BusinessInquiryErrorType.FAIL_DELETE);
        }
        
        saveInquiryPort.deleteById(id);
    }
}
