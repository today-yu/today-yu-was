package com.todayyu.application.inquiry.usecase;

import com.todayyu.application.common.annotation.Usecase;
import com.todayyu.application.inquiry.dto.command.SubmitInquiryCommand;
import com.todayyu.application.inquiry.dto.result.SubmitInquiryResult;
import com.todayyu.application.inquiry.port.save.SaveInquiryPort;
import com.todayyu.domain.inquiry.entity.Inquiry;
import com.todayyu.domain.inquiry.vo.InquiryId;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Usecase
@RequiredArgsConstructor
public class SubmitInquiryUsecase {
    private final SaveInquiryPort saveInquiryPort;

    /**
     * 문의를 답변 대기({@code WAITING}) 상태로 저장하는 메서드
     *
     * @param command 문의 작성 요청 DTO
     * @return 문의 작성 결과 DTO
     */
    @Transactional
    public SubmitInquiryResult submit(SubmitInquiryCommand command) {
        Inquiry inquiry = Inquiry.withoutId(
                command.memberId(),
                command.contactEmail(),
                command.title(),
                command.content(),
                LocalDateTime.now()
        );
        
        InquiryId id = saveInquiryPort.save(inquiry).getId();
        
        return new SubmitInquiryResult(id);
    }
}
