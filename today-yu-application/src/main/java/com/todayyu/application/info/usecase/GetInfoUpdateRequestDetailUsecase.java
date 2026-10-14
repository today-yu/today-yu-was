package com.todayyu.application.info.usecase;

import com.todayyu.application.common.annotation.Usecase;
import com.todayyu.application.info.dto.query.GetInfoUpdateRequestDetailQuery;
import com.todayyu.application.info.dto.result.InfoUpdateRequestDetailResult;
import com.todayyu.application.info.error.BusinessInfoUpdateRequestErrorType;
import com.todayyu.application.info.port.read.ReadInfoPort;
import com.todayyu.application.info.port.read.ReadInfoUpdateRequestPort;
import com.todayyu.common.exception.CustomException;
import com.todayyu.domain.info.entity.Info;
import com.todayyu.domain.info.entity.InfoUpdateRequest;
import com.todayyu.domain.info.vo.InfoId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Usecase
@RequiredArgsConstructor
public class GetInfoUpdateRequestDetailUsecase {
    private final ReadInfoUpdateRequestPort readInfoUpdateRequestPort;
    private final ReadInfoPort readInfoPort;
    
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
        
        Info info = readLinkedInfo(infoUpdateRequest);
        
        return InfoUpdateRequestDetailResult.from(infoUpdateRequest, info);
    }
    
    /**
     * 정보 수정 요청({@link InfoUpdateRequest})와 연결된 정보({@link Info})를 조회하는 메서드
     *
     * <p> 연결된 정보 ID가 {@code null}인 경우에는 {@code null}을 반환한다. </p>
     *
     * <p> 연결된 정보가 존재하지 않는 경우에는, 에러 로그 출력 후 {@code null}을 반환한다. </p>
     *
     * @param request 정보 수정 요청
     * @return 정보
     */
    private Info readLinkedInfo(InfoUpdateRequest request) {
        InfoId infoId = request.getInfoId();
        
        if (infoId == null) {
            return null;
        }
        
        return readInfoPort.readById(infoId).orElseGet(() -> {
                log.warn(
                        "[Error/Data] 정보 수정 요청에 연결된 정보가 존재하지 않습니다. (requestId: {} / infoId: {})",
                        request.getId().getValue(),
                        infoId.getValue()
                );
                
                return null;
            });
    }
}
