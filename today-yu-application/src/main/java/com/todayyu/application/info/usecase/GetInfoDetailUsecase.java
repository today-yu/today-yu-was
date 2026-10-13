package com.todayyu.application.info.usecase;

import com.todayyu.application.common.annotation.Usecase;
import com.todayyu.application.info.dto.GetInfoDetailQuery;
import com.todayyu.application.info.dto.InfoDetailResult;
import com.todayyu.application.info.dto.InfoRegistrationRequestView;
import com.todayyu.application.info.dto.InfoUpdateRequestView;
import com.todayyu.application.info.error.BusinessInfoErrorType;
import com.todayyu.application.info.port.read.ReadHelpfulMarkPort;
import com.todayyu.application.info.port.read.ReadInfoPort;
import com.todayyu.application.info.port.read.ReadInfoRegistrationRequestPort;
import com.todayyu.application.info.port.read.ReadInfoUpdateRequestPort;
import com.todayyu.application.info.port.read.ReadInfoViewCountPort;
import com.todayyu.application.info.port.save.SaveInfoViewCountPort;
import com.todayyu.common.exception.CustomException;
import com.todayyu.domain.info.entity.Info;
import com.todayyu.domain.info.entity.InfoViewCount;
import com.todayyu.domain.info.vo.InfoId;
import com.todayyu.domain.info.vo.InfoRequestStatus;
import com.todayyu.domain.member.vo.MemberId;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Usecase
@RequiredArgsConstructor
public class GetInfoDetailUsecase {
    private final ReadInfoPort readInfoPort;
    private final ReadInfoViewCountPort readInfoViewCountPort;
    private final ReadHelpfulMarkPort readHelpfulMarkPort;
    private final ReadInfoRegistrationRequestPort readInfoRegistrationRequestPort;
    private final ReadInfoUpdateRequestPort readInfoUpdateRequestPort;
    private final SaveInfoViewCountPort saveInfoViewCountPort;
    
    /**
     * 정보의 상세 내용을 조회하는 메서드
     *
     * <p> 정보 상세 조회에 성공한 뒤, 해당 정보의 조회수를 증가한다. </p>
     *
     * <p> 정보 상세 내용, 조회수, '도움이 되는 정보에요' 수, 요청을 보낸 회원이 '도움이 되는 정보에요'를 눌렀는지 여부,
     *     해당 정보 작성에 기여한 정보 등록 요청, 정보 수정 요청을 조회한 뒤 조합하여 반환한다. </p>
     *
     * @param query 정보 상세 내용 조회용 DTO
     * @return 정보 상세 내용
     */
    @Transactional
    public InfoDetailResult getInfoDetail(GetInfoDetailQuery query) {
        InfoId infoId = query.infoId();
        MemberId memberId = query.memberId();
        
        Info info = readInfoPort.readById(infoId)
                .orElseThrow(() -> new CustomException(BusinessInfoErrorType.NOT_FOUND));
        
        InfoViewCount infoViewCount = readInfoViewCountPort.readByInfoId(infoId)
                .orElseThrow(() -> new IllegalStateException("정보 조회수가 존재하지 않습니다. infoId = " + infoId.getValue()));
        
        long helpfulMarkCount = readHelpfulMarkPort.countByInfoId(infoId);
        boolean helpfulMarked = readHelpfulMarkPort.existsByInfoIdAndMemberId(infoId, memberId);
        
        List<InfoRegistrationRequestView> infoRegistrationRequestViews
                = readInfoRegistrationRequestPort.readByInfoIdAndStatus(infoId, InfoRequestStatus.ACCEPTED);
        List<InfoUpdateRequestView> infoUpdateRequestViews
                = readInfoUpdateRequestPort.readByInfoIdAndStatus(infoId, InfoRequestStatus.ACCEPTED);
        
        saveInfoViewCountPort.increase(infoId);
        
        return InfoDetailResult.from(
                info,
                infoViewCount,
                helpfulMarkCount,
                helpfulMarked,
                infoRegistrationRequestViews,
                infoUpdateRequestViews
        );
    }
}
