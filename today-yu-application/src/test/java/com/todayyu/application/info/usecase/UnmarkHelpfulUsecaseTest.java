package com.todayyu.application.info.usecase;

import com.todayyu.application.info.dto.command.UnmarkHelpfulCommand;
import com.todayyu.application.info.error.BusinessInfoErrorType;
import com.todayyu.application.info.port.read.ReadInfoPort;
import com.todayyu.application.info.port.save.SaveHelpfulMarkPort;
import com.todayyu.common.exception.CustomException;
import com.todayyu.domain.info.vo.InfoId;
import com.todayyu.domain.member.vo.MemberId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class UnmarkHelpfulUsecaseTest {
    private static final InfoId INFO_ID = new InfoId(10L);
    private static final MemberId MEMBER_ID = new MemberId(1L);
    private static final UnmarkHelpfulCommand COMMAND = new UnmarkHelpfulCommand(INFO_ID, MEMBER_ID);

    @Mock
    private ReadInfoPort readInfoPort;

    @Mock
    private SaveHelpfulMarkPort saveHelpfulMarkPort;

    @InjectMocks
    private UnmarkHelpfulUsecase unmarkHelpfulUsecase;

    @Test
    @DisplayName("정보가 존재하면 입력한 정보 ID와 회원 ID로 '도움이 되는 정보에요' 삭제를 요청한다")
    void unmarkHelpful() {
        // given
        given(readInfoPort.existsById(INFO_ID)).willReturn(true);

        // when
        unmarkHelpfulUsecase.unmarkHelpful(COMMAND);

        // then
        then(saveHelpfulMarkPort).should().deleteByInfoIdAndMemberId(INFO_ID, MEMBER_ID);
    }

    @Test
    @DisplayName("정보가 존재하지 않으면 예외가 발생하고 '도움이 되는 정보에요'를 삭제하지 않는다")
    void unmarkHelpfulWithNonExistentInfo() {
        // given
        given(readInfoPort.existsById(INFO_ID)).willReturn(false);

        // when
        CustomException exception = assertThrows(CustomException.class,
                () -> unmarkHelpfulUsecase.unmarkHelpful(COMMAND));

        // then
        assertEquals(BusinessInfoErrorType.NOT_FOUND, exception.getErrorType());
        then(saveHelpfulMarkPort).should(never()).deleteByInfoIdAndMemberId(any(), any());
    }
}
