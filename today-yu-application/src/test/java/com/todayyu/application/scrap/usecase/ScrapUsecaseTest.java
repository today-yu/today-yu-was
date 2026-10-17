package com.todayyu.application.scrap.usecase;

import com.todayyu.application.info.error.BusinessInfoErrorType;
import com.todayyu.application.info.port.read.ReadInfoPort;
import com.todayyu.application.scrap.dto.command.ScrapCommand;
import com.todayyu.application.scrap.port.read.ReadScrapPort;
import com.todayyu.application.scrap.port.save.SaveScrapPort;
import com.todayyu.common.exception.CustomException;
import com.todayyu.domain.info.vo.InfoId;
import com.todayyu.domain.member.vo.MemberId;
import com.todayyu.domain.scrap.entity.Scrap;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class ScrapUsecaseTest {
    private static final InfoId INFO_ID = new InfoId(10L);
    private static final MemberId MEMBER_ID = new MemberId(1L);
    private static final ScrapCommand COMMAND = new ScrapCommand(INFO_ID, MEMBER_ID);

    @Mock
    private ReadInfoPort readInfoPort;

    @Mock
    private ReadScrapPort readScrapPort;

    @Mock
    private SaveScrapPort saveScrapPort;

    @InjectMocks
    private ScrapUsecase scrapUsecase;

    @Captor
    private ArgumentCaptor<Scrap> scrapCaptor;

    @Test
    @DisplayName("스크랩하지 않은 정보이면 입력한 정보, 회원, 현재 시각으로 스크랩을 저장한다")
    void scrap() {
        // given
        given(readInfoPort.existsById(INFO_ID)).willReturn(true);
        given(readScrapPort.existsByInfoIdAndMemberId(INFO_ID, MEMBER_ID)).willReturn(false);
        LocalDateTime before = LocalDateTime.now();

        // when
        scrapUsecase.scrap(COMMAND);

        // then
        LocalDateTime after = LocalDateTime.now();
        then(saveScrapPort).should().save(scrapCaptor.capture());
        Scrap saved = scrapCaptor.getValue();
        assertNull(saved.getId());
        assertEquals(INFO_ID, saved.getInfoId());
        assertEquals(MEMBER_ID, saved.getMemberId());
        assertFalse(saved.getScrappedAt().isBefore(before));
        assertFalse(saved.getScrappedAt().isAfter(after));
    }

    @Test
    @DisplayName("이미 스크랩한 정보이면 예외 없이 스크랩을 다시 저장하지 않는다")
    void scrapAlreadyScrappedInfo() {
        // given
        given(readInfoPort.existsById(INFO_ID)).willReturn(true);
        given(readScrapPort.existsByInfoIdAndMemberId(INFO_ID, MEMBER_ID)).willReturn(true);

        // when
        scrapUsecase.scrap(COMMAND);

        // then
        then(saveScrapPort).should(never()).save(any());
    }

    @Test
    @DisplayName("정보가 존재하지 않으면 예외가 발생하고 스크랩을 조회·저장하지 않는다")
    void scrapWithNonExistentInfo() {
        // given
        given(readInfoPort.existsById(INFO_ID)).willReturn(false);

        // when
        CustomException exception = assertThrows(CustomException.class,
                () -> scrapUsecase.scrap(COMMAND));

        // then
        assertEquals(BusinessInfoErrorType.NOT_FOUND, exception.getErrorType());
        then(readScrapPort).should(never()).existsByInfoIdAndMemberId(any(), any());
        then(saveScrapPort).should(never()).save(any());
    }
}
