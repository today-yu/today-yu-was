package com.todayyu.application.info.usecase;

import com.todayyu.application.common.annotation.Usecase;
import com.todayyu.application.info.dto.SubmitInfoUpdateRequestCommand;
import com.todayyu.application.info.dto.SubmitInfoUpdateRequestResult;
import com.todayyu.application.info.error.BusinessInfoErrorType;
import com.todayyu.application.info.port.read.ReadInfoPort;
import com.todayyu.application.info.port.save.SaveInfoUpdateRequestPort;
import com.todayyu.common.exception.CustomException;
import com.todayyu.domain.info.entity.InfoUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Usecase
@RequiredArgsConstructor
public class SubmitInfoUpdateRequestUsecase {
    private final ReadInfoPort readInfoPort;
    private final SaveInfoUpdateRequestPort saveInfoUpdateRequestPort;
    
    /**
     * 정보 수정 요청을 저장하는 메서드
     *
     * @param command 정보 수정 요청 저장 요청 DTO
     * @return 정보 수정 요청 저장 결과 DTO
     * @throws CustomException {@link BusinessInfoErrorType#NOT_FOUND} - 정보가 존재하지 않을 때
     */
    @Transactional
    public SubmitInfoUpdateRequestResult submit(SubmitInfoUpdateRequestCommand command) {
        if (!readInfoPort.existsById(command.infoId())) {
            throw new CustomException(BusinessInfoErrorType.NOT_FOUND);
        }
        
        InfoUpdateRequest infoUpdateRequest = InfoUpdateRequest.withoutId(
                command.requesterId(),
                command.infoId(),
                command.content(),
                command.contactEmail(),
                command.observedAt(),
                LocalDateTime.now()
        );
        
        InfoUpdateRequest savedInfoUpdateRequest = saveInfoUpdateRequestPort.save(infoUpdateRequest);
        
        return new SubmitInfoUpdateRequestResult(savedInfoUpdateRequest.getId());
    }
}
