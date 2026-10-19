package com.todayyu.apis.user.info.controller;

import com.todayyu.apis.common.exception.GlobalExceptionHandler;
import com.todayyu.apis.common.security.principal.CustomUserDetails;
import com.todayyu.application.common.dto.CursorResult;
import com.todayyu.application.info.dto.result.ContributionResult;
import com.todayyu.application.info.dto.result.ContributionType;
import com.todayyu.application.info.dto.query.GetInfoDetailQuery;
import com.todayyu.application.info.dto.query.GetInfosQuery;
import com.todayyu.application.info.dto.InfoCursor;
import com.todayyu.application.info.dto.result.InfoDetailResult;
import com.todayyu.application.info.dto.InfoSortType;
import com.todayyu.application.info.dto.result.InfoSummaryResult;
import com.todayyu.application.info.error.BusinessInfoErrorType;
import com.todayyu.application.info.usecase.GetInfoDetailUsecase;
import com.todayyu.application.info.usecase.GetInfosUsecase;
import com.todayyu.common.error.CommonErrorType;
import com.todayyu.common.exception.CustomException;
import com.todayyu.domain.info.vo.InfoCategory;
import com.todayyu.domain.info.vo.InfoContent;
import com.todayyu.domain.info.vo.InfoId;
import com.todayyu.domain.info.vo.InfoTitle;
import com.todayyu.domain.info.vo.ViewCount;
import com.todayyu.domain.member.vo.MemberId;
import com.todayyu.domain.member.vo.Nickname;
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
import static org.mockito.Mockito.never;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class InfoControllerTest {
    private static final String INFOS_URL = "/api/v1/infos";
    private static final String INFO_DETAIL_URL = "/api/v1/infos/{infoId}";
    private static final Long INFO_ID = 1L;
    private static final Long MEMBER_ID = 2L;
    private static final Long CONTRIBUTOR_ID = 3L;
    private static final String CONTRIBUTOR_NICKNAME = "용감한 영대인 1";
    private static final Long REGISTRATION_REQUEST_ID = 10L;
    private static final Long UPDATE_REQUEST_ID = 20L;
    private static final LocalDateTime PUBLISHED_AT = LocalDateTime.of(2026, 10, 1, 9, 0, 30);
    private static final LocalDateTime UPDATED_AT = LocalDateTime.of(2026, 10, 5, 9, 0, 30);
    private static final LocalDateTime REGISTRATION_REQUESTED_AT = LocalDateTime.of(2026, 9, 30, 9, 0, 30);
    private static final LocalDateTime UPDATE_REQUESTED_AT = LocalDateTime.of(2026, 10, 4, 9, 0, 30);
    private static final Long CURSOR_INFO_ID = 100L;
    private static final LocalDateTime CURSOR_UPDATED_AT = LocalDateTime.of(2026, 10, 10, 9, 0, 30);
    private static final Long FIRST_INFO_ID = 99L;
    private static final Long SECOND_INFO_ID = 98L;
    private static final LocalDateTime FIRST_UPDATED_AT = LocalDateTime.of(2026, 10, 9, 9, 0, 30);
    private static final LocalDateTime SECOND_UPDATED_AT = LocalDateTime.of(2026, 10, 8, 9, 0, 30);

    @Mock
    private GetInfosUsecase getInfosUsecase;

    @Mock
    private GetInfoDetailUsecase getInfoDetailUsecase;

    @InjectMocks
    private InfoController infoController;

    @Captor
    private ArgumentCaptor<GetInfosQuery> getInfosQueryCaptor;

    @Captor
    private ArgumentCaptor<GetInfoDetailQuery> getInfoDetailQueryCaptor;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(infoController)
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
    @DisplayName("정보 목록 조회 요청 시 회원 ID·카테고리·정렬 기준·커서·조회 크기로 조회를 요청하고, 정보 목록(스크랩 여부 포함)과 다음 커서를 원시값으로 변환해 반환한다")
    void getInfos() throws Exception {
        // given
        InfoCursor nextCursor = new InfoCursor(SECOND_UPDATED_AT, new InfoId(SECOND_INFO_ID));
        given(getInfosUsecase.getInfos(any())).willReturn(new CursorResult<>(
                List.of(
                        infoSummaryResult(FIRST_INFO_ID, "수강신청 일정", 3L, FIRST_UPDATED_AT, true),
                        infoSummaryResult(SECOND_INFO_ID, "장학금 신청 안내", 0L, SECOND_UPDATED_AT, false)
                ),
                true,
                nextCursor
        ));

        // when
        mockMvc.perform(get(INFOS_URL)
                        .param("category", "ACADEMIC")
                        .param("sortType", "LATEST")
                        .param("cursorUpdatedAt", CURSOR_UPDATED_AT.toString())
                        .param("cursorInfoId", String.valueOf(CURSOR_INFO_ID))
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("정보 조회에 성공하였습니다."))
                .andExpect(jsonPath("$.data.contents.length()").value(2))
                .andExpect(jsonPath("$.data.contents[0].infoId").value(FIRST_INFO_ID))
                .andExpect(jsonPath("$.data.contents[0].category").value(InfoCategory.ACADEMIC.name()))
                .andExpect(jsonPath("$.data.contents[0].title").value("수강신청 일정"))
                .andExpect(jsonPath("$.data.contents[0].helpfulMarkCount").value(3))
                .andExpect(jsonPath("$.data.contents[0].updatedAt").value(FIRST_UPDATED_AT.toString()))
                .andExpect(jsonPath("$.data.contents[0].scrapped").value(true))
                .andExpect(jsonPath("$.data.contents[1].infoId").value(SECOND_INFO_ID))
                .andExpect(jsonPath("$.data.contents[1].helpfulMarkCount").value(0))
                .andExpect(jsonPath("$.data.contents[1].scrapped").value(false))
                .andExpect(jsonPath("$.data.hasNext").value(true))
                .andExpect(jsonPath("$.data.nextCursor.updatedAt").value(SECOND_UPDATED_AT.toString()))
                .andExpect(jsonPath("$.data.nextCursor.infoId").value(SECOND_INFO_ID));

        // then
        then(getInfosUsecase).should().getInfos(getInfosQueryCaptor.capture());
        GetInfosQuery query = getInfosQueryCaptor.getValue();
        assertEquals(new MemberId(MEMBER_ID), query.memberId());
        assertEquals(InfoCategory.ACADEMIC, query.category());
        assertEquals(InfoSortType.LATEST, query.sortType());
        assertEquals(new InfoCursor(CURSOR_UPDATED_AT, new InfoId(CURSOR_INFO_ID)), query.cursor());
        assertEquals(2, query.size());
    }

    @Test
    @DisplayName("첫 요청(카테고리·커서·조회 크기 없음)이면 카테고리와 커서 없이 기본 조회 크기로 조회를 요청하고, 다음 데이터가 없으면 다음 커서는 null이다")
    void getInfosWithFirstRequest() throws Exception {
        // given
        given(getInfosUsecase.getInfos(any())).willReturn(new CursorResult<>(List.of(), false, null));

        // when
        mockMvc.perform(get(INFOS_URL)
                        .param("sortType", "OLDEST"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.contents").isArray())
                .andExpect(jsonPath("$.data.contents.length()").value(0))
                .andExpect(jsonPath("$.data.hasNext").value(false))
                .andExpect(jsonPath("$.data.nextCursor").value(nullValue()));

        // then
        then(getInfosUsecase).should().getInfos(getInfosQueryCaptor.capture());
        GetInfosQuery query = getInfosQueryCaptor.getValue();
        assertNull(query.category());
        assertEquals(InfoSortType.OLDEST, query.sortType());
        assertNull(query.cursor());
        assertEquals(GetInfosQuery.DEFAULT_SIZE, query.size());
    }

    @Test
    @DisplayName("정렬 기준이 없으면 유효성 검증 에러를 응답하고 조회를 하지 않는다")
    void getInfosWithoutSortType() throws Exception {
        // given & when
        mockMvc.perform(get(INFOS_URL))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.errorCode").value(CommonErrorType.VALIDATION_ERROR.getErrorCode()))
                .andExpect(jsonPath("$.errors.sortType").value("정보 정렬 타입은 비어있을 수 없습니다."));

        // then
        then(getInfosUsecase).should(never()).getInfos(any());
    }

    @Test
    @DisplayName("커서 값이 하나만 존재하면 유효성 검증 에러를 응답하고 조회를 하지 않는다")
    void getInfosWithPartialCursor() throws Exception {
        // given & when
        mockMvc.perform(get(INFOS_URL)
                        .param("sortType", "LATEST")
                        .param("cursorUpdatedAt", CURSOR_UPDATED_AT.toString()))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.errorCode").value(CommonErrorType.VALIDATION_ERROR.getErrorCode()))
                .andExpect(jsonPath("$.errors.cursorValid").value("커서 값은 모두 존재하거나 모두 비어있어야 합니다."));

        // then
        then(getInfosUsecase).should(never()).getInfos(any());
    }

    @ParameterizedTest
    @ValueSource(ints = {GetInfosQuery.MIN_SIZE - 1, GetInfosQuery.MAX_SIZE + 1})
    @DisplayName("조회 크기가 허용 범위를 벗어나면 유효성 검증 에러를 응답하고 조회를 하지 않는다")
    void getInfosWithOutOfRangeSize(int size) throws Exception {
        // given & when
        mockMvc.perform(get(INFOS_URL)
                        .param("sortType", "LATEST")
                        .param("size", String.valueOf(size)))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.errorCode").value(CommonErrorType.VALIDATION_ERROR.getErrorCode()))
                .andExpect(jsonPath("$.errors.size").exists());

        // then
        then(getInfosUsecase).should(never()).getInfos(any());
    }

    @Test
    @DisplayName("존재하지 않는 카테고리로 요청하면 유효성 검증 에러를 응답하고 조회를 하지 않는다")
    void getInfosWithInvalidCategory() throws Exception {
        // given & when
        mockMvc.perform(get(INFOS_URL)
                        .param("category", "INVALID")
                        .param("sortType", "LATEST"))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.errorCode").value(CommonErrorType.VALIDATION_ERROR.getErrorCode()))
                .andExpect(jsonPath("$.errors.category").exists());

        // then
        then(getInfosUsecase).should(never()).getInfos(any());
    }

    @Test
    @DisplayName("정보 상세 조회 요청 시 정보 ID와 인증된 회원 ID로 조회를 요청하고, 정보 상세 내용을 원시값으로 변환해 반환한다")
    void getInfoDetail() throws Exception {
        // given
        given(getInfoDetailUsecase.getInfoDetail(any())).willReturn(infoDetailResult(
                true,
                true,
                List.of(
                        contribution(ContributionType.UPDATE, UPDATE_REQUEST_ID, UPDATE_REQUESTED_AT),
                        contribution(ContributionType.REGISTRATION, REGISTRATION_REQUEST_ID, REGISTRATION_REQUESTED_AT)
                )
        ));

        // when
        mockMvc.perform(get(INFO_DETAIL_URL, INFO_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("정보 상세 조회에 성공하였습니다."))
                .andExpect(jsonPath("$.data.id").value(INFO_ID))
                .andExpect(jsonPath("$.data.category").value(InfoCategory.ACADEMIC.name()))
                .andExpect(jsonPath("$.data.title").value("수강신청 일정"))
                .andExpect(jsonPath("$.data.content").value("수강신청은 10월 20일부터 진행됩니다."))
                .andExpect(jsonPath("$.data.publishedAt").value(PUBLISHED_AT.toString()))
                .andExpect(jsonPath("$.data.updatedAt").value(UPDATED_AT.toString()))
                .andExpect(jsonPath("$.data.viewCount").value(10))
                .andExpect(jsonPath("$.data.helpfulMarkCount").value(5))
                .andExpect(jsonPath("$.data.helpfulMarked").value(true))
                .andExpect(jsonPath("$.data.scrapped").value(true))
                .andExpect(jsonPath("$.data.contributions.length()").value(2))
                .andExpect(jsonPath("$.data.contributions[0].memberId").value(CONTRIBUTOR_ID))
                .andExpect(jsonPath("$.data.contributions[0].nickname").value(CONTRIBUTOR_NICKNAME))
                .andExpect(jsonPath("$.data.contributions[0].type").value(ContributionType.UPDATE.name()))
                .andExpect(jsonPath("$.data.contributions[0].requestId").value(UPDATE_REQUEST_ID))
                .andExpect(jsonPath("$.data.contributions[0].requestedAt").value(UPDATE_REQUESTED_AT.toString()))
                .andExpect(jsonPath("$.data.contributions[1].type").value(ContributionType.REGISTRATION.name()))
                .andExpect(jsonPath("$.data.contributions[1].requestId").value(REGISTRATION_REQUEST_ID))
                .andExpect(jsonPath("$.data.contributions[1].requestedAt").value(REGISTRATION_REQUESTED_AT.toString()));

        // then
        then(getInfoDetailUsecase).should().getInfoDetail(getInfoDetailQueryCaptor.capture());
        assertEquals(new InfoId(INFO_ID), getInfoDetailQueryCaptor.getValue().infoId());
        assertEquals(new MemberId(MEMBER_ID), getInfoDetailQueryCaptor.getValue().memberId());
    }

    @Test
    @DisplayName("기여 내역이 없고 '도움이 되는 정보에요'를 누르지 않았으며 스크랩하지 않은 정보는 빈 기여자 목록과 false를 반환한다")
    void getInfoDetailWithoutContributionsAndHelpfulMarkAndScrap() throws Exception {
        // given
        given(getInfoDetailUsecase.getInfoDetail(any())).willReturn(infoDetailResult(false, false, List.of()));

        // when & then
        mockMvc.perform(get(INFO_DETAIL_URL, INFO_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.helpfulMarked").value(false))
                .andExpect(jsonPath("$.data.scrapped").value(false))
                .andExpect(jsonPath("$.data.contributions").isArray())
                .andExpect(jsonPath("$.data.contributions.length()").value(0));
    }

    @Test
    @DisplayName("존재하지 않는 정보를 조회하면 정보 없음 에러를 응답한다")
    void getInfoDetailWithNotExistingInfo() throws Exception {
        // given
        given(getInfoDetailUsecase.getInfoDetail(any()))
                .willThrow(new CustomException(BusinessInfoErrorType.NOT_FOUND));

        // when & then
        mockMvc.perform(get(INFO_DETAIL_URL, INFO_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value(BusinessInfoErrorType.NOT_FOUND.getErrorCode()))
                .andExpect(jsonPath("$.message").value(BusinessInfoErrorType.NOT_FOUND.getMessage()));
    }

    @Test
    @DisplayName("정보 ID가 숫자가 아니면 잘못된 요청 에러를 응답하고 조회를 하지 않는다")
    void getInfoDetailWithInvalidInfoId() throws Exception {
        // given & when
        mockMvc.perform(get(INFO_DETAIL_URL, "invalid-id"))
                .andExpect(status().isBadRequest());

        // then
        then(getInfoDetailUsecase).should(never()).getInfoDetail(any());
    }

    private InfoSummaryResult infoSummaryResult(
            Long infoId,
            String title,
            long helpfulMarkCount,
            LocalDateTime updatedAt,
            boolean scrapped
    ) {
        return InfoSummaryResult.builder()
                .infoId(new InfoId(infoId))
                .category(InfoCategory.ACADEMIC)
                .title(new InfoTitle(title))
                .helpfulMarkCount(helpfulMarkCount)
                .updatedAt(updatedAt)
                .scrapped(scrapped)
                .build();
    }

    private InfoDetailResult infoDetailResult(
            boolean helpfulMarked,
            boolean scrapped,
            List<ContributionResult> contributions
    ) {
        return InfoDetailResult.builder()
                .id(new InfoId(INFO_ID))
                .category(InfoCategory.ACADEMIC)
                .title(new InfoTitle("수강신청 일정"))
                .content(new InfoContent("수강신청은 10월 20일부터 진행됩니다."))
                .publishedAt(PUBLISHED_AT)
                .updatedAt(UPDATED_AT)
                .viewCount(new ViewCount(10L))
                .helpfulMarkCount(5L)
                .helpfulMarked(helpfulMarked)
                .scrapped(scrapped)
                .contributions(contributions)
                .build();
    }

    private ContributionResult contribution(ContributionType type, Long requestId, LocalDateTime requestedAt) {
        return ContributionResult.builder()
                .memberId(new MemberId(CONTRIBUTOR_ID))
                .nickname(new Nickname(CONTRIBUTOR_NICKNAME))
                .type(type)
                .requestId(requestId)
                .requestedAt(requestedAt)
                .build();
    }
}
