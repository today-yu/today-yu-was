package com.todayyu.application.info.usecase;

import com.todayyu.application.common.annotation.Usecase;
import com.todayyu.application.info.dto.query.GetInfoRegistrationRequestDetailQuery;
import com.todayyu.application.info.dto.result.InfoRegistrationRequestDetailResult;
import com.todayyu.application.info.error.BusinessInfoRegistrationRequestErrorType;
import com.todayyu.application.info.port.read.ReadInfoRegistrationRequestPort;
import com.todayyu.common.exception.CustomException;
import com.todayyu.domain.info.entity.InfoRegistrationRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@Usecase
@RequiredArgsConstructor
public class GetInfoRegistrationRequestDetailUsecase {
    private final ReadInfoRegistrationRequestPort readInfoRegistrationRequestPort;
    
    /**
     * 정보 등록 요청 상세 조회 메서드
     *
     * @param query 정보 등록 요청 상세 조회 요청 DTO
     * @return 정보 등록 요청 상세 정보
     * @throws CustomException 정보 등록 요청 상세 조회에 실패한 경우
     * <ul>
     *     <li>{@link BusinessInfoRegistrationRequestErrorType#NOT_FOUND} - 정보 등록 요청이 존재하지 않을 경우</li>
     *     <li>{@link BusinessInfoRegistrationRequestErrorType#FORBIDDEN} - 정보 등록 요청 작성자가 아닌 경우</li>
     * </ul>
     */
    @Transactional(readOnly = true)
    public InfoRegistrationRequestDetailResult getInfoRegistrationRequestDetail(
            GetInfoRegistrationRequestDetailQuery query
    ) {
        InfoRegistrationRequest infoRegistrationRequest = readInfoRegistrationRequestPort.readById(query.id())
                .orElseThrow(() -> new CustomException(BusinessInfoRegistrationRequestErrorType.NOT_FOUND));
        
        if (!infoRegistrationRequest.isRequester(query.memberId())) {
            throw new CustomException(BusinessInfoRegistrationRequestErrorType.FORBIDDEN);
        }
        
        return InfoRegistrationRequestDetailResult.from(infoRegistrationRequest);
    }
}
