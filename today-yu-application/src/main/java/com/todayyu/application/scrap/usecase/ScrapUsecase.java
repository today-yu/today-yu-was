package com.todayyu.application.scrap.usecase;

import com.todayyu.application.common.annotation.Usecase;
import com.todayyu.application.info.error.BusinessInfoErrorType;
import com.todayyu.application.info.port.read.ReadInfoPort;
import com.todayyu.application.scrap.dto.command.ScrapCommand;
import com.todayyu.application.scrap.port.read.ReadScrapPort;
import com.todayyu.application.scrap.port.save.SaveScrapPort;
import com.todayyu.common.exception.CustomException;
import com.todayyu.domain.info.vo.InfoId;
import com.todayyu.domain.member.vo.MemberId;
import com.todayyu.domain.scrap.entity.Scrap;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Usecase
@RequiredArgsConstructor
public class ScrapUsecase {
    private final ReadInfoPort readInfoPort;
    private final ReadScrapPort readScrapPort;
    private final SaveScrapPort saveScrapPort;
    
    /**
     * 회원이 관심있는 정보를 스크랩하는 메서드
     *
     * <p> 회원이 이미 해당 정보를 스크랩한 경우에는 새로 저장하지 않는다. </p>
     *
     * @param command 스크랩 요청 DTO
     * @throws CustomException {@link BusinessInfoErrorType#NOT_FOUND} - 스크랩할 정보가 존재하지 않을 경우
     */
    @Transactional
    public void scrap(ScrapCommand command) {
        InfoId infoId = command.infoId();
        MemberId memberId = command.memberId();
        
        if (!readInfoPort.existsById(infoId)) {
            throw new CustomException(BusinessInfoErrorType.NOT_FOUND);
        }
        
        if (!readScrapPort.existsByInfoIdAndMemberId(infoId, memberId)) {
            saveScrapPort.save(Scrap.withoutId(infoId, memberId, LocalDateTime.now()));
        }
    }
}
