package com.todayyu.application.info.usecase;

import com.todayyu.application.common.annotation.Usecase;
import com.todayyu.application.info.dto.command.SubmitInfoRegistrationRequestCommand;
import com.todayyu.application.info.dto.result.SubmitInfoRegistrationRequestResult;
import com.todayyu.application.info.port.save.SaveInfoRegistrationRequestPort;
import com.todayyu.domain.info.entity.InfoRegistrationRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Usecase
@RequiredArgsConstructor
public class SubmitInfoRegistrationRequestUsecase {
    private final SaveInfoRegistrationRequestPort saveInfoRegistrationRequestPort;
    
    /**
     * 정보 등록 요청을 저장하는 메서드
     *
     * @param command 정보 등록 요청 저장 요청 DTO
     * @return 정보 등록 요청 저장 결과 DTO
     */
    @Transactional
    public SubmitInfoRegistrationRequestResult submit(SubmitInfoRegistrationRequestCommand command) {
        InfoRegistrationRequest infoRegistrationRequest = InfoRegistrationRequest.withoutId(
                command.requesterId(),
                command.category(),
                command.content(),
                command.contactEmail(),
                LocalDateTime.now()
        );
        
        InfoRegistrationRequest savedInfoRegistrationRequest
                = saveInfoRegistrationRequestPort.save(infoRegistrationRequest);
        
        return new SubmitInfoRegistrationRequestResult(savedInfoRegistrationRequest.getId());
    }
}
