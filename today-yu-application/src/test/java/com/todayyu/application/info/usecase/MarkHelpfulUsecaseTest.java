package com.todayyu.application.info.usecase;

import com.todayyu.application.info.dto.command.MarkHelpfulCommand;
import com.todayyu.application.info.error.BusinessInfoErrorType;
import com.todayyu.application.info.port.read.ReadHelpfulMarkPort;
import com.todayyu.application.info.port.read.ReadInfoPort;
import com.todayyu.application.info.port.save.SaveHelpfulMarkPort;
import com.todayyu.common.exception.CustomException;
import com.todayyu.domain.info.entity.HelpfulMark;
import com.todayyu.domain.info.vo.InfoId;
import com.todayyu.domain.member.vo.MemberId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class MarkHelpfulUsecaseTest {
    private static final InfoId INFO_ID = new InfoId(10L);
    private static final MemberId MEMBER_ID = new MemberId(1L);
    private static final MarkHelpfulCommand COMMAND = new MarkHelpfulCommand(INFO_ID, MEMBER_ID);

    @Mock
    private ReadInfoPort readInfoPort;

    @Mock
    private ReadHelpfulMarkPort readHelpfulMarkPort;

    @Mock
    private SaveHelpfulMarkPort saveHelpfulMarkPort;

    @InjectMocks
    private MarkHelpfulUsecase markHelpfulUsecase;

    @Captor
    private ArgumentCaptor<HelpfulMark> helpfulMarkCaptor;

    @Test
    @DisplayName("'도움이 되는 정보에요'를 표시하지 않은 정보이면 입력한 정보, 회원으로 '도움이 되는 정보에요'를 저장한다")
    void markHelpful() {
        // given
        given(readInfoPort.existsById(INFO_ID)).willReturn(true);
        given(readHelpfulMarkPort.existsByInfoIdAndMemberId(INFO_ID, MEMBER_ID)).willReturn(false);

        // when
        markHelpfulUsecase.markHelpful(COMMAND);

        // then
        then(saveHelpfulMarkPort).should().save(helpfulMarkCaptor.capture());
        HelpfulMark saved = helpfulMarkCaptor.getValue();
        assertNull(saved.getId());
        assertEquals(INFO_ID, saved.getInfoId());
        assertEquals(MEMBER_ID, saved.getMemberId());
    }

    @Test
    @DisplayName("이미 '도움이 되는 정보에요'를 표시한 정보이면 예외 없이 다시 저장하지 않는다")
    void markHelpfulAlreadyMarkedInfo() {
        // given
        given(readInfoPort.existsById(INFO_ID)).willReturn(true);
        given(readHelpfulMarkPort.existsByInfoIdAndMemberId(INFO_ID, MEMBER_ID)).willReturn(true);

        // when
        markHelpfulUsecase.markHelpful(COMMAND);

        // then
        then(saveHelpfulMarkPort).should(never()).save(any());
    }

    @Test
    @DisplayName("정보가 존재하지 않으면 예외가 발생하고 '도움이 되는 정보에요'를 조회·저장하지 않는다")
    void markHelpfulWithNonExistentInfo() {
        // given
        given(readInfoPort.existsById(INFO_ID)).willReturn(false);

        // when
        CustomException exception = assertThrows(CustomException.class,
                () -> markHelpfulUsecase.markHelpful(COMMAND));

        // then
        assertEquals(BusinessInfoErrorType.NOT_FOUND, exception.getErrorType());
        then(readHelpfulMarkPort).should(never()).existsByInfoIdAndMemberId(any(), any());
        then(saveHelpfulMarkPort).should(never()).save(any());
    }
}
