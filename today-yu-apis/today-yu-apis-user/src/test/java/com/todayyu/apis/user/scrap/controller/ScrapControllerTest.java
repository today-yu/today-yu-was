package com.todayyu.apis.user.scrap.controller;

import com.todayyu.apis.common.exception.GlobalExceptionHandler;
import com.todayyu.apis.common.security.principal.CustomUserDetails;
import com.todayyu.application.common.dto.CursorResult;
import com.todayyu.application.info.error.BusinessInfoErrorType;
import com.todayyu.application.scrap.dto.ScrapCursor;
import com.todayyu.application.scrap.dto.ScrapSortType;
import com.todayyu.application.scrap.dto.command.CancelScrapCommand;
import com.todayyu.application.scrap.dto.command.ScrapCommand;
import com.todayyu.application.scrap.dto.query.GetScrapsQuery;
import com.todayyu.application.scrap.dto.result.ScrapResult;
import com.todayyu.application.scrap.usecase.CancelScrapUsecase;
import com.todayyu.application.scrap.usecase.GetScrapsUsecase;
import com.todayyu.application.scrap.usecase.ScrapUsecase;
import com.todayyu.common.error.CommonErrorType;
import com.todayyu.common.exception.CustomException;
import com.todayyu.domain.info.vo.InfoCategory;
import com.todayyu.domain.info.vo.InfoId;
import com.todayyu.domain.info.vo.InfoTitle;
import com.todayyu.domain.member.vo.MemberId;
import com.todayyu.domain.member.vo.Role;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ScrapControllerTest {
    private static final String SCRAP_URL = "/api/v1/infos/{infoId}/scrap";
    private static final Long MEMBER_ID = 1L;
    private static final Long INFO_ID = 5L;
    private static final String SCRAPS_URL = "/api/v1/infos/scraps";
    private static final Long CURSOR_INFO_ID = 100L;
    private static final LocalDateTime CURSOR_UPDATED_AT = LocalDateTime.of(2026, 10, 10, 9, 0, 30);
    private static final Long FIRST_INFO_ID = 99L;
    private static final Long SECOND_INFO_ID = 98L;
    private static final LocalDateTime FIRST_UPDATED_AT = LocalDateTime.of(2026, 10, 9, 9, 0, 30);
    private static final LocalDateTime SECOND_UPDATED_AT = LocalDateTime.of(2026, 10, 8, 9, 0, 30);

    @Mock
    private GetScrapsUsecase getScrapsUsecase;

    @Mock
    private ScrapUsecase scrapUsecase;

    @Mock
    private CancelScrapUsecase cancelScrapUsecase;

    @InjectMocks
    private ScrapController scrapController;

    @Captor
    private ArgumentCaptor<GetScrapsQuery> getScrapsQueryCaptor;

    @Captor
    private ArgumentCaptor<ScrapCommand> commandCaptor;

    @Captor
    private ArgumentCaptor<CancelScrapCommand> cancelCommandCaptor;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(scrapController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .build();

        CustomUserDetails userDetails = new CustomUserDetails(new MemberId(MEMBER_ID), Role.MEMBER);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities())
        );
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("스크랩 목록 조회 요청 시 회원 ID·카테고리·정렬 기준·커서·조회 크기로 조회를 요청하고, 스크랩한 정보 목록과 다음 커서를 원시값으로 변환해 반환한다")
    void getScraps() throws Exception {
        // given
        ScrapCursor nextCursor = new ScrapCursor(SECOND_UPDATED_AT, new InfoId(SECOND_INFO_ID));
        given(getScrapsUsecase.getScraps(any())).willReturn(new CursorResult<>(
                List.of(
                        scrapResult(FIRST_INFO_ID, "수강신청 일정", 3L, FIRST_UPDATED_AT),
                        scrapResult(SECOND_INFO_ID, "장학금 신청 안내", 0L, SECOND_UPDATED_AT)
                ),
                true,
                nextCursor
        ));

        // when
        mockMvc.perform(get(SCRAPS_URL)
                        .param("category", "ACADEMIC")
                        .param("sortType", "LATEST")
                        .param("cursorUpdatedAt", CURSOR_UPDATED_AT.toString())
                        .param("cursorInfoId", String.valueOf(CURSOR_INFO_ID))
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("스크랩 목록 조회에 성공하였습니다."))
                .andExpect(jsonPath("$.data.contents.length()").value(2))
                .andExpect(jsonPath("$.data.contents[0].infoId").value(FIRST_INFO_ID))
                .andExpect(jsonPath("$.data.contents[0].category").value(InfoCategory.ACADEMIC.name()))
                .andExpect(jsonPath("$.data.contents[0].title").value("수강신청 일정"))
                .andExpect(jsonPath("$.data.contents[0].helpfulMarkCount").value(3))
                .andExpect(jsonPath("$.data.contents[0].updatedAt").value(FIRST_UPDATED_AT.toString()))
                .andExpect(jsonPath("$.data.contents[1].infoId").value(SECOND_INFO_ID))
                .andExpect(jsonPath("$.data.contents[1].helpfulMarkCount").value(0))
                .andExpect(jsonPath("$.data.hasNext").value(true))
                .andExpect(jsonPath("$.data.nextCursor.updatedAt").value(SECOND_UPDATED_AT.toString()))
                .andExpect(jsonPath("$.data.nextCursor.infoId").value(SECOND_INFO_ID));

        // then
        then(getScrapsUsecase).should().getScraps(getScrapsQueryCaptor.capture());
        GetScrapsQuery query = getScrapsQueryCaptor.getValue();
        assertEquals(new MemberId(MEMBER_ID), query.memberId());
        assertEquals(InfoCategory.ACADEMIC, query.category());
        assertEquals(ScrapSortType.LATEST, query.sortType());
        assertEquals(new ScrapCursor(CURSOR_UPDATED_AT, new InfoId(CURSOR_INFO_ID)), query.cursor());
        assertEquals(2, query.size());
    }

    @Test
    @DisplayName("첫 요청(카테고리·커서·조회 크기 없음)이면 카테고리와 커서 없이 기본 조회 크기로 조회를 요청하고, 다음 데이터가 없으면 다음 커서는 null이다")
    void getScrapsWithFirstRequest() throws Exception {
        // given
        given(getScrapsUsecase.getScraps(any())).willReturn(new CursorResult<>(List.of(), false, null));

        // when
        mockMvc.perform(get(SCRAPS_URL)
                        .param("sortType", "OLDEST"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.contents").isArray())
                .andExpect(jsonPath("$.data.contents.length()").value(0))
                .andExpect(jsonPath("$.data.hasNext").value(false))
                .andExpect(jsonPath("$.data.nextCursor").value(nullValue()));

        // then
        then(getScrapsUsecase).should().getScraps(getScrapsQueryCaptor.capture());
        GetScrapsQuery query = getScrapsQueryCaptor.getValue();
        assertNull(query.category());
        assertEquals(ScrapSortType.OLDEST, query.sortType());
        assertNull(query.cursor());
        assertEquals(GetScrapsQuery.DEFAULT_SIZE, query.size());
    }

    @Test
    @DisplayName("정렬 기준이 없으면 유효성 검증 에러를 응답하고 조회를 하지 않는다")
    void getScrapsWithoutSortType() throws Exception {
        // given & when
        mockMvc.perform(get(SCRAPS_URL))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.errorCode").value(CommonErrorType.VALIDATION_ERROR.getErrorCode()))
                .andExpect(jsonPath("$.errors.sortType").value("정렬 기준은 비어있을 수 없습니다."));

        // then
        then(getScrapsUsecase).should(never()).getScraps(any());
    }

    @Test
    @DisplayName("커서 값이 하나만 존재하면 유효성 검증 에러를 응답하고 조회를 하지 않는다")
    void getScrapsWithPartialCursor() throws Exception {
        // given & when
        mockMvc.perform(get(SCRAPS_URL)
                        .param("sortType", "LATEST")
                        .param("cursorInfoId", String.valueOf(CURSOR_INFO_ID)))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.errorCode").value(CommonErrorType.VALIDATION_ERROR.getErrorCode()))
                .andExpect(jsonPath("$.errors.cursorValid").exists());

        // then
        then(getScrapsUsecase).should(never()).getScraps(any());
    }

    @ParameterizedTest
    @ValueSource(ints = {GetScrapsQuery.MIN_SIZE - 1, GetScrapsQuery.MAX_SIZE + 1})
    @DisplayName("조회 크기가 허용 범위를 벗어나면 유효성 검증 에러를 응답하고 조회를 하지 않는다")
    void getScrapsWithOutOfRangeSize(int size) throws Exception {
        // given & when
        mockMvc.perform(get(SCRAPS_URL)
                        .param("sortType", "LATEST")
                        .param("size", String.valueOf(size)))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.errorCode").value(CommonErrorType.VALIDATION_ERROR.getErrorCode()))
                .andExpect(jsonPath("$.errors.size").exists());

        // then
        then(getScrapsUsecase).should(never()).getScraps(any());
    }

    @Test
    @DisplayName("존재하지 않는 카테고리로 요청하면 유효성 검증 에러를 응답하고 조회를 하지 않는다")
    void getScrapsWithInvalidCategory() throws Exception {
        // given & when
        mockMvc.perform(get(SCRAPS_URL)
                        .param("category", "INVALID")
                        .param("sortType", "LATEST"))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.errorCode").value(CommonErrorType.VALIDATION_ERROR.getErrorCode()))
                .andExpect(jsonPath("$.errors.category").exists());

        // then
        then(getScrapsUsecase).should(never()).getScraps(any());
    }

    @Test
    @DisplayName("스크랩 요청 시 경로의 정보 ID와 인증된 회원 ID로 스크랩을 요청하고, 성공 메시지를 반환한다")
    void scrap() throws Exception {
        // when
        mockMvc.perform(put(SCRAP_URL, INFO_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("스크랩에 성공하였습니다."))
                .andExpect(jsonPath("$.data").value(nullValue()));

        // then
        then(scrapUsecase).should().scrap(commandCaptor.capture());
        ScrapCommand command = commandCaptor.getValue();
        assertEquals(new InfoId(INFO_ID), command.infoId());
        assertEquals(new MemberId(MEMBER_ID), command.memberId());
    }

    @Test
    @DisplayName("스크랩할 정보가 존재하지 않으면 정보 없음 에러를 응답한다")
    void scrapWithNonExistentInfo() throws Exception {
        // given
        willThrow(new CustomException(BusinessInfoErrorType.NOT_FOUND))
                .given(scrapUsecase).scrap(any());

        // when & then
        mockMvc.perform(put(SCRAP_URL, INFO_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value(BusinessInfoErrorType.NOT_FOUND.getErrorCode()))
                .andExpect(jsonPath("$.message").value(BusinessInfoErrorType.NOT_FOUND.getMessage()));
    }

    @Test
    @DisplayName("정보 ID가 숫자가 아니면 잘못된 요청 에러를 응답하고 스크랩하지 않는다")
    void scrapWithInvalidInfoId() throws Exception {
        // given & when
        mockMvc.perform(put(SCRAP_URL, "invalid-id"))
                .andExpect(status().isBadRequest());

        // then
        then(scrapUsecase).should(never()).scrap(any());
    }

    @Test
    @DisplayName("스크랩 해제 요청 시 경로의 정보 ID와 인증된 회원 ID로 스크랩 해제를 요청하고, 성공 메시지를 반환한다")
    void cancelScrap() throws Exception {
        // when
        mockMvc.perform(delete(SCRAP_URL, INFO_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("스크랩 해제에 성공하였습니다."))
                .andExpect(jsonPath("$.data").value(nullValue()));

        // then
        then(cancelScrapUsecase).should().cancelScrap(cancelCommandCaptor.capture());
        CancelScrapCommand command = cancelCommandCaptor.getValue();
        assertEquals(new InfoId(INFO_ID), command.infoId());
        assertEquals(new MemberId(MEMBER_ID), command.memberId());
    }

    @Test
    @DisplayName("스크랩을 해제할 정보가 존재하지 않으면 정보 없음 에러를 응답한다")
    void cancelScrapWithNonExistentInfo() throws Exception {
        // given
        willThrow(new CustomException(BusinessInfoErrorType.NOT_FOUND))
                .given(cancelScrapUsecase).cancelScrap(any());

        // when & then
        mockMvc.perform(delete(SCRAP_URL, INFO_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value(BusinessInfoErrorType.NOT_FOUND.getErrorCode()))
                .andExpect(jsonPath("$.message").value(BusinessInfoErrorType.NOT_FOUND.getMessage()));
    }

    @Test
    @DisplayName("정보 ID가 숫자가 아니면 잘못된 요청 에러를 응답하고 스크랩을 해제하지 않는다")
    void cancelScrapWithInvalidInfoId() throws Exception {
        // given & when
        mockMvc.perform(delete(SCRAP_URL, "invalid-id"))
                .andExpect(status().isBadRequest());

        // then
        then(cancelScrapUsecase).should(never()).cancelScrap(any());
    }

    private ScrapResult scrapResult(Long infoId, String title, long helpfulMarkCount, LocalDateTime updatedAt) {
        return new ScrapResult(
                new InfoId(infoId),
                InfoCategory.ACADEMIC,
                new InfoTitle(title),
                helpfulMarkCount,
                updatedAt
        );
    }
}
