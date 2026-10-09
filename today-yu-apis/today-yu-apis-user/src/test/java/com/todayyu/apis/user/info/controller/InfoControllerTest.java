package com.todayyu.apis.user.info.controller;

import com.todayyu.apis.common.exception.GlobalExceptionHandler;
import com.todayyu.apis.common.security.principal.CustomUserDetails;
import com.todayyu.application.info.dto.ContributionResult;
import com.todayyu.application.info.dto.ContributionType;
import com.todayyu.application.info.dto.GetInfoDetailQuery;
import com.todayyu.application.info.dto.InfoDetailResult;
import com.todayyu.application.info.error.BusinessInfoErrorType;
import com.todayyu.application.info.usecase.GetInfoDetailUsecase;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class InfoControllerTest {
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

    @Mock
    private GetInfoDetailUsecase getInfoDetailUsecase;

    @InjectMocks
    private InfoController infoController;

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
    @DisplayName("정보 상세 조회 요청 시 정보 ID와 인증된 회원 ID로 조회를 요청하고, 정보 상세 내용을 원시값으로 변환해 반환한다")
    void getInfoDetail() throws Exception {
        // given
        given(getInfoDetailUsecase.getInfoDetail(any())).willReturn(infoDetailResult(
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
    @DisplayName("기여 내역이 없고 '도움이 되는 정보에요'를 누르지 않은 정보는 빈 기여자 목록과 false를 반환한다")
    void getInfoDetailWithoutContributionsAndHelpfulMark() throws Exception {
        // given
        given(getInfoDetailUsecase.getInfoDetail(any())).willReturn(infoDetailResult(false, List.of()));

        // when & then
        mockMvc.perform(get(INFO_DETAIL_URL, INFO_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.helpfulMarked").value(false))
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

    private InfoDetailResult infoDetailResult(boolean helpfulMarked, List<ContributionResult> contributions) {
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
