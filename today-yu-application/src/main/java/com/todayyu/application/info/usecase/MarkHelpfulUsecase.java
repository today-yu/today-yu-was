package com.todayyu.application.info.usecase;

import com.todayyu.application.common.annotation.Usecase;
import com.todayyu.application.info.dto.command.MarkHelpfulCommand;
import com.todayyu.application.info.error.BusinessInfoErrorType;
import com.todayyu.application.info.port.read.ReadHelpfulMarkPort;
import com.todayyu.application.info.port.read.ReadInfoPort;
import com.todayyu.application.info.port.save.SaveHelpfulMarkPort;
import com.todayyu.common.exception.CustomException;
import com.todayyu.domain.info.entity.HelpfulMark;
import com.todayyu.domain.info.vo.InfoId;
import com.todayyu.domain.member.vo.MemberId;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@Usecase
@RequiredArgsConstructor
public class MarkHelpfulUsecase {
    private final ReadInfoPort readInfoPort;
    private final ReadHelpfulMarkPort readHelpfulMarkPort;
    private final SaveHelpfulMarkPort saveHelpfulMarkPort;
    
    /**
     * 회원이 정보에 '도움이 되는 정보에요'를 표시하는 메서드
     *
     * <p> 회원이 이미 해당 정보에 '도움이 되는 정보에요'를 표시한 경우에는 새로 저장하지 않는다. </p>
     *
     * @param command '도움이 되는 정보에요' 표시 요청 DTO
     * @throws CustomException {@link BusinessInfoErrorType#NOT_FOUND} - 표시할 정보가 존재하지 않을 경우
     */
    @Transactional
    public void markHelpful(MarkHelpfulCommand command) {
        InfoId infoId = command.infoId();
        MemberId memberId = command.memberId();
        
        if (!readInfoPort.existsById(infoId)) {
            throw new CustomException(BusinessInfoErrorType.NOT_FOUND);
        }
        
        if (!readHelpfulMarkPort.existsByInfoIdAndMemberId(infoId, memberId)) {
            HelpfulMark helpfulMark = HelpfulMark.withoutId(infoId, memberId);
            saveHelpfulMarkPort.save(helpfulMark);
        }
    }
}
