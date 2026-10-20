package com.todayyu.application.inquiry.usecase;

import com.todayyu.application.common.annotation.Usecase;
import com.todayyu.application.inquiry.dto.command.UpdateInquiryCommand;
import com.todayyu.application.inquiry.error.BusinessInquiryErrorType;
import com.todayyu.application.inquiry.port.read.ReadInquiryPort;
import com.todayyu.application.inquiry.port.save.SaveInquiryPort;
import com.todayyu.common.exception.CustomException;
import com.todayyu.domain.inquiry.entity.Inquiry;
import com.todayyu.domain.inquiry.error.InquiryErrorType;
import com.todayyu.domain.inquiry.vo.InquiryId;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Usecase
@RequiredArgsConstructor
public class UpdateInquiryUsecase {
    private final ReadInquiryPort readInquiryPort;
    private final SaveInquiryPort saveInquiryPort;
    
    /**
     * 문의 수정 메서드
     *
     * @param command 문의 수정 요청 DTO
     * @throws CustomException 문의 수정에 실패한 경우
     * <ul>
     *     <li>{@link BusinessInquiryErrorType#NOT_FOUND} - 문의가 존재하지 않을 경우</li>
     *     <li>{@link BusinessInquiryErrorType#FORBIDDEN} - 문의 작성자가 아닌 경우</li>
     *     <li>{@link InquiryErrorType#FAIL_UPDATE} - 답변 대기 중 상태의 문의가 아닌 경우</li>
     * </ul>
     */
    @Transactional
    public void update(UpdateInquiryCommand command) {
        InquiryId id = command.id();
        
        Inquiry inquiry = readInquiryPort.readById(id)
                .orElseThrow(() -> new CustomException(BusinessInquiryErrorType.NOT_FOUND));
        
        if (!inquiry.isAuthor(command.memberId())) {
            throw new CustomException(BusinessInquiryErrorType.FORBIDDEN);
        }
        
        if (!inquiry.canUpdate()) {
            throw new CustomException(InquiryErrorType.FAIL_UPDATE);
        }
        
        LocalDateTime now = LocalDateTime.now();
        inquiry.update(command.title(), command.content(), command.contactEmail(), now);
        
        saveInquiryPort.update(inquiry);
    }
}
