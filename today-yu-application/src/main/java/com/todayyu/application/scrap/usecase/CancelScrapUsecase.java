package com.todayyu.application.scrap.usecase;

import com.todayyu.application.common.annotation.Usecase;
import com.todayyu.application.info.error.BusinessInfoErrorType;
import com.todayyu.application.info.port.read.ReadInfoPort;
import com.todayyu.application.scrap.dto.command.CancelScrapCommand;
import com.todayyu.application.scrap.port.save.SaveScrapPort;
import com.todayyu.common.exception.CustomException;
import com.todayyu.domain.info.vo.InfoId;
import com.todayyu.domain.member.vo.MemberId;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@Usecase
@RequiredArgsConstructor
public class CancelScrapUsecase {
    private final ReadInfoPort readInfoPort;
    private final SaveScrapPort saveScrapPort;
    
    /**
     * 회원이 스크랩한 정보의 스크랩을 해제하는 메서드
     *
     * <p> 회원이 해당 정보를 스크랩하지 않은 경우에는 아무것도 하지 않는다. </p>
     *
     * @param command 스크랩 해제 요청 DTO
     * @throws CustomException {@link BusinessInfoErrorType#NOT_FOUND} - 스크랩을 해제할 정보가 존재하지 않을 경우
     */
    @Transactional
    public void cancelScrap(CancelScrapCommand command) {
        InfoId infoId = command.infoId();
        MemberId memberId = command.memberId();
        
        if (!readInfoPort.existsById(infoId)) {
            throw new CustomException(BusinessInfoErrorType.NOT_FOUND);
        }
        
        saveScrapPort.deleteByInfoIdAndMemberId(infoId, memberId);
    }
}
