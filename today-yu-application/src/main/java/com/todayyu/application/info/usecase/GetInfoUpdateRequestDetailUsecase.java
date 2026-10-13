package com.todayyu.application.info.usecase;

import com.todayyu.application.common.annotation.Usecase;
import com.todayyu.application.info.dto.query.GetInfoUpdateRequestDetailQuery;
import com.todayyu.application.info.dto.result.InfoUpdateRequestDetailResult;
import com.todayyu.application.info.error.BusinessInfoUpdateRequestErrorType;
import com.todayyu.application.info.port.read.ReadInfoUpdateRequestPort;
import com.todayyu.common.exception.CustomException;
import com.todayyu.domain.info.entity.InfoUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@Usecase
@RequiredArgsConstructor
public class GetInfoUpdateRequestDetailUsecase {
    private final ReadInfoUpdateRequestPort readInfoUpdateRequestPort;
    
    /**
     * 정보 수정 요청 상세 조회 메서드
     *
     * @param query 정보 수정 요청 상세 조회 요청 DTO
     * @return 정보 수정 요청 상세 정보
     * @throws CustomException 정보 수정 요청 상세 조회에 실패한 경우
     * <ul>
     *     <li>{@link BusinessInfoUpdateRequestErrorType#NOT_FOUND} - 정보 수정 요청이 존재하지 않을 경우</li>
     *     <li>{@link BusinessInfoUpdateRequestErrorType#FORBIDDEN} - 정보 수정 요청 작성자가 아닌 경우</li>
     * </ul>
     */
    @Transactional(readOnly = true)
    public InfoUpdateRequestDetailResult getInfoUpdateRequestDetail(GetInfoUpdateRequestDetailQuery query) {
        InfoUpdateRequest infoUpdateRequest = readInfoUpdateRequestPort.readById(query.id())
                .orElseThrow(() -> new CustomException(BusinessInfoUpdateRequestErrorType.NOT_FOUND));
        
        if (!infoUpdateRequest.isRequester(query.memberId())) {
            throw new CustomException(BusinessInfoUpdateRequestErrorType.FORBIDDEN);
        }
        
        return InfoUpdateRequestDetailResult.from(infoUpdateRequest);
    }
}
