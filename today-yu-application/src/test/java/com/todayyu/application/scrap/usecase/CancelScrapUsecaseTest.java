package com.todayyu.application.scrap.usecase;

import com.todayyu.application.info.error.BusinessInfoErrorType;
import com.todayyu.application.info.port.read.ReadInfoPort;
import com.todayyu.application.scrap.dto.command.CancelScrapCommand;
import com.todayyu.application.scrap.port.save.SaveScrapPort;
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
class CancelScrapUsecaseTest {
    private static final InfoId INFO_ID = new InfoId(10L);
    private static final MemberId MEMBER_ID = new MemberId(1L);
    private static final CancelScrapCommand COMMAND = new CancelScrapCommand(INFO_ID, MEMBER_ID);

    @Mock
    private ReadInfoPort readInfoPort;

    @Mock
    private SaveScrapPort saveScrapPort;

    @InjectMocks
    private CancelScrapUsecase cancelScrapUsecase;

    @Test
    @DisplayName("정보가 존재하면 입력한 정보 ID와 회원 ID로 스크랩 삭제를 요청한다")
    void cancelScrap() {
        // given
        given(readInfoPort.existsById(INFO_ID)).willReturn(true);

        // when
        cancelScrapUsecase.cancelScrap(COMMAND);

        // then
        then(saveScrapPort).should().deleteByInfoIdAndMemberId(INFO_ID, MEMBER_ID);
    }

    @Test
    @DisplayName("정보가 존재하지 않으면 예외가 발생하고 스크랩을 삭제하지 않는다")
    void cancelScrapWithNonExistentInfo() {
        // given
        given(readInfoPort.existsById(INFO_ID)).willReturn(false);

        // when
        CustomException exception = assertThrows(CustomException.class,
                () -> cancelScrapUsecase.cancelScrap(COMMAND));

        // then
        assertEquals(BusinessInfoErrorType.NOT_FOUND, exception.getErrorType());
        then(saveScrapPort).should(never()).deleteByInfoIdAndMemberId(any(), any());
    }
}
