package com.todayyu.apis.user.info.controller;

import com.todayyu.apis.common.exception.GlobalExceptionHandler;
import com.todayyu.apis.common.security.principal.CustomUserDetails;
import com.todayyu.application.common.dto.CursorResult;
import com.todayyu.application.info.dto.command.SubmitInfoUpdateRequestCommand;
import com.todayyu.application.info.dto.query.GetInfoUpdateRequestDetailQuery;
import com.todayyu.application.info.dto.query.GetInfoUpdateRequestsQuery;
import com.todayyu.application.info.dto.result.InfoUpdateRequestDetailResult;
import com.todayyu.application.info.dto.result.InfoUpdateRequestSummaryResult;
import com.todayyu.application.info.dto.result.LinkedInfoResult;
import com.todayyu.application.info.dto.result.SubmitInfoUpdateRequestResult;
import com.todayyu.application.info.error.BusinessInfoErrorType;
import com.todayyu.application.info.error.BusinessInfoUpdateRequestErrorType;
import com.todayyu.application.info.dto.InfoUpdateRequestCursor;
import com.todayyu.application.info.port.dto.InfoUpdateRequestSortType;
import com.todayyu.application.info.usecase.GetInfoUpdateRequestDetailUsecase;
import com.todayyu.application.info.usecase.GetInfoUpdateRequestsUsecase;
import com.todayyu.application.info.usecase.SubmitInfoUpdateRequestUsecase;
import com.todayyu.common.error.CommonErrorType;
import com.todayyu.common.exception.CustomException;
import com.todayyu.domain.common.vo.Email;
import com.todayyu.domain.info.vo.InfoCategory;
import com.todayyu.domain.info.vo.InfoId;
import com.todayyu.domain.info.vo.InfoRequestStatus;
import com.todayyu.domain.info.vo.InfoTitle;
import com.todayyu.domain.info.vo.InfoUpdateRequestContent;
import com.todayyu.domain.info.vo.InfoUpdateRequestId;
import com.todayyu.domain.info.vo.RejectionReason;
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
import org.springframework.http.MediaType;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class InfoUpdateRequestControllerTest {
    private static final String SUBMIT_URL = "/api/v1/infos/{infoId}/update-requests";
    private static final String DETAIL_URL = "/api/v1/infos/update-requests/{requestId}";
    private static final String LIST_URL = "/api/v1/infos/update-requests";
    private static final Long MEMBER_ID = 1L;
    private static final Long INFO_ID = 5L;
    private static final String INFO_CATEGORY = "ACADEMIC";
    private static final String INFO_TITLE = "수강신청 일정";
    private static final LocalDateTime INFO_UPDATED_AT = LocalDateTime.of(2026, 9, 15, 12, 0, 30);
    private static final Long REQUEST_ID = 10L;
    private static final String CONTENT = "수강신청 일정이 10월 21일로 변경되었습니다.";
    private static final String CONTACT_EMAIL = "requester@example.com";
    private static final String OBSERVED_AT = "2026-10-01T09:00:00";
    private static final LocalDateTime OBSERVED_AT_TIME = LocalDateTime.of(2026, 10, 1, 9, 0, 0);
    private static final LocalDateTime REQUESTED_AT = LocalDateTime.of(2026, 10, 1, 9, 0, 30);
    private static final String REJECTION_REASON = "사실 관계를 확인할 수 없습니다.";
    private static final Long FIRST_REQUEST_ID = 9L;
    private static final Long SECOND_REQUEST_ID = 8L;
    private static final LocalDateTime FIRST_REQUESTED_AT = LocalDateTime.of(2026, 10, 9, 9, 0, 30);
    private static final LocalDateTime SECOND_REQUESTED_AT = LocalDateTime.of(2026, 10, 8, 9, 0, 30);
    private static final LocalDateTime CURSOR_REQUESTED_AT = LocalDateTime.of(2026, 10, 10, 9, 0, 30);
    private static final Long CURSOR_REQUEST_ID = 10L;

    @Mock
    private GetInfoUpdateRequestsUsecase getInfoUpdateRequestsUsecase;

    @Mock
    private GetInfoUpdateRequestDetailUsecase getInfoUpdateRequestDetailUsecase;

    @Mock
    private SubmitInfoUpdateRequestUsecase submitInfoUpdateRequestUsecase;

    @InjectMocks
    private InfoUpdateRequestController infoUpdateRequestController;

    @Captor
    private ArgumentCaptor<SubmitInfoUpdateRequestCommand> commandCaptor;

    @Captor
    private ArgumentCaptor<GetInfoUpdateRequestDetailQuery> detailQueryCaptor;

    @Captor
    private ArgumentCaptor<GetInfoUpdateRequestsQuery> listQueryCaptor;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(infoUpdateRequestController)
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
    @DisplayName("정보 수정 요청 목록 조회 요청 시 인증된 회원 ID·카테고리·정렬 기준·커서·조회 크기로 조회를 요청하고, 목록과 다음 커서를 원시값으로 변환해 반환한다")
    void getInfoUpdateRequests() throws Exception {
        // given
        InfoUpdateRequestCursor nextCursor = new InfoUpdateRequestCursor(
                SECOND_REQUESTED_AT,
                new InfoUpdateRequestId(SECOND_REQUEST_ID)
        );
        given(getInfoUpdateRequestsUsecase.getInfoUpdateRequests(any())).willReturn(new CursorResult<>(
                List.of(
                        summaryResult(FIRST_REQUEST_ID, INFO_TITLE, InfoRequestStatus.PENDING, FIRST_REQUESTED_AT),
                        summaryResult(SECOND_REQUEST_ID, "2학기 학사 일정", InfoRequestStatus.REJECTED, SECOND_REQUESTED_AT)
                ),
                true,
                nextCursor
        ));

        // when
        mockMvc.perform(get(LIST_URL)
                        .param("category", INFO_CATEGORY)
                        .param("sortType", "LATEST")
                        .param("cursorRequestedAt", CURSOR_REQUESTED_AT.toString())
                        .param("cursorId", String.valueOf(CURSOR_REQUEST_ID))
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("정보 수정 요청 조회에 성공하였습니다."))
                .andExpect(jsonPath("$.data.contents.length()").value(2))
                .andExpect(jsonPath("$.data.contents[0].id").value(FIRST_REQUEST_ID))
                .andExpect(jsonPath("$.data.contents[0].category").value(INFO_CATEGORY))
                .andExpect(jsonPath("$.data.contents[0].title").value(INFO_TITLE))
                .andExpect(jsonPath("$.data.contents[0].status").value(InfoRequestStatus.PENDING.name()))
                .andExpect(jsonPath("$.data.contents[0].requestedAt").value(FIRST_REQUESTED_AT.toString()))
                .andExpect(jsonPath("$.data.contents[1].id").value(SECOND_REQUEST_ID))
                .andExpect(jsonPath("$.data.contents[1].title").value("2학기 학사 일정"))
                .andExpect(jsonPath("$.data.contents[1].status").value(InfoRequestStatus.REJECTED.name()))
                .andExpect(jsonPath("$.data.hasNext").value(true))
                .andExpect(jsonPath("$.data.nextCursor.requestedAt").value(SECOND_REQUESTED_AT.toString()))
                .andExpect(jsonPath("$.data.nextCursor.id").value(SECOND_REQUEST_ID));

        // then
        then(getInfoUpdateRequestsUsecase).should().getInfoUpdateRequests(listQueryCaptor.capture());
        GetInfoUpdateRequestsQuery query = listQueryCaptor.getValue();
        assertEquals(new MemberId(MEMBER_ID), query.requesterId());
        assertEquals(InfoCategory.valueOf(INFO_CATEGORY), query.category());
        assertEquals(InfoUpdateRequestSortType.LATEST, query.sortType());
        assertEquals(
                new InfoUpdateRequestCursor(CURSOR_REQUESTED_AT, new InfoUpdateRequestId(CURSOR_REQUEST_ID)),
                query.cursor()
        );
        assertEquals(2, query.size());
    }

    @Test
    @DisplayName("첫 요청(카테고리·커서·조회 크기 없음)이면 카테고리와 커서 없이 기본 조회 크기로 조회를 요청하고, 다음 데이터가 없으면 다음 커서는 null이다")
    void getInfoUpdateRequestsWithFirstRequest() throws Exception {
        // given
        given(getInfoUpdateRequestsUsecase.getInfoUpdateRequests(any()))
                .willReturn(new CursorResult<>(List.of(), false, null));

        // when
        mockMvc.perform(get(LIST_URL)
                        .param("sortType", "OLDEST"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.contents").isArray())
                .andExpect(jsonPath("$.data.contents.length()").value(0))
                .andExpect(jsonPath("$.data.hasNext").value(false))
                .andExpect(jsonPath("$.data.nextCursor").value(nullValue()));

        // then
        then(getInfoUpdateRequestsUsecase).should().getInfoUpdateRequests(listQueryCaptor.capture());
        GetInfoUpdateRequestsQuery query = listQueryCaptor.getValue();
        assertEquals(new MemberId(MEMBER_ID), query.requesterId());
        assertNull(query.category());
        assertEquals(InfoUpdateRequestSortType.OLDEST, query.sortType());
        assertNull(query.cursor());
        assertEquals(GetInfoUpdateRequestsQuery.DEFAULT_SIZE, query.size());
    }

    @Test
    @DisplayName("정렬 기준이 없으면 유효성 검증 에러를 응답하고 조회를 하지 않는다")
    void getInfoUpdateRequestsWithoutSortType() throws Exception {
        // given & when
        mockMvc.perform(get(LIST_URL))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.errorCode").value(CommonErrorType.VALIDATION_ERROR.getErrorCode()))
                .andExpect(jsonPath("$.errors.sortType").value("정렬 기준은 비어있을 수 없습니다."));

        // then
        then(getInfoUpdateRequestsUsecase).should(never()).getInfoUpdateRequests(any());
    }

    @Test
    @DisplayName("커서 값이 하나만 존재하면 유효성 검증 에러를 응답하고 조회를 하지 않는다")
    void getInfoUpdateRequestsWithPartialCursor() throws Exception {
        // given & when
        mockMvc.perform(get(LIST_URL)
                        .param("sortType", "LATEST")
                        .param("cursorId", String.valueOf(CURSOR_REQUEST_ID)))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.errorCode").value(CommonErrorType.VALIDATION_ERROR.getErrorCode()))
                .andExpect(jsonPath("$.errors.cursorValid").value("커서 값은 모두 존재하거나 모두 비어있어야 합니다."));

        // then
        then(getInfoUpdateRequestsUsecase).should(never()).getInfoUpdateRequests(any());
    }

    @ParameterizedTest
    @ValueSource(ints = {GetInfoUpdateRequestsQuery.MIN_SIZE - 1, GetInfoUpdateRequestsQuery.MAX_SIZE + 1})
    @DisplayName("조회 크기가 허용 범위를 벗어나면 유효성 검증 에러를 응답하고 조회를 하지 않는다")
    void getInfoUpdateRequestsWithOutOfRangeSize(int size) throws Exception {
        // given & when
        mockMvc.perform(get(LIST_URL)
                        .param("sortType", "LATEST")
                        .param("size", String.valueOf(size)))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.errorCode").value(CommonErrorType.VALIDATION_ERROR.getErrorCode()))
                .andExpect(jsonPath("$.errors.size").exists());

        // then
        then(getInfoUpdateRequestsUsecase).should(never()).getInfoUpdateRequests(any());
    }

    @Test
    @DisplayName("존재하지 않는 카테고리로 요청하면 유효성 검증 에러를 응답하고 조회를 하지 않는다")
    void getInfoUpdateRequestsWithInvalidCategory() throws Exception {
        // given & when
        mockMvc.perform(get(LIST_URL)
                        .param("category", "INVALID")
                        .param("sortType", "LATEST"))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.errorCode").value(CommonErrorType.VALIDATION_ERROR.getErrorCode()))
                .andExpect(jsonPath("$.errors.category").exists());

        // then
        then(getInfoUpdateRequestsUsecase).should(never()).getInfoUpdateRequests(any());
    }

    @Test
    @DisplayName("정보 수정 요청 상세 조회 요청 시 요청 ID와 인증된 회원 ID로 조회를 요청하고, 연결된 정보를 포함한 상세 내용을 원시값으로 변환해 반환한다")
    void getInfoUpdateRequestDetail() throws Exception {
        // given
        given(getInfoUpdateRequestDetailUsecase.getInfoUpdateRequestDetail(any()))
                .willReturn(detailResult(InfoRequestStatus.PENDING, linkedInfoResult(), null));

        // when
        mockMvc.perform(get(DETAIL_URL, REQUEST_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("정보 수정 요청 상세 조회에 성공하였습니다."))
                .andExpect(jsonPath("$.data.id").value(REQUEST_ID))
                .andExpect(jsonPath("$.data.requesterId").value(MEMBER_ID))
                .andExpect(jsonPath("$.data.content").value(CONTENT))
                .andExpect(jsonPath("$.data.contactEmail").value(CONTACT_EMAIL))
                .andExpect(jsonPath("$.data.observedAt").value(OBSERVED_AT))
                .andExpect(jsonPath("$.data.requestedAt").value(REQUESTED_AT.toString()))
                .andExpect(jsonPath("$.data.status").value(InfoRequestStatus.PENDING.name()))
                .andExpect(jsonPath("$.data.info.id").value(INFO_ID))
                .andExpect(jsonPath("$.data.info.category").value(INFO_CATEGORY))
                .andExpect(jsonPath("$.data.info.title").value(INFO_TITLE))
                .andExpect(jsonPath("$.data.info.updatedAt").value(INFO_UPDATED_AT.toString()))
                .andExpect(jsonPath("$.data.rejectionReason").value(nullValue()));

        // then
        then(getInfoUpdateRequestDetailUsecase).should()
                .getInfoUpdateRequestDetail(detailQueryCaptor.capture());
        assertEquals(new InfoUpdateRequestId(REQUEST_ID), detailQueryCaptor.getValue().id());
        assertEquals(new MemberId(MEMBER_ID), detailQueryCaptor.getValue().memberId());
    }

    @Test
    @DisplayName("반려(REJECTED)된 정보 수정 요청을 조회하면 반려 사유를 반환한다")
    void getRejectedInfoUpdateRequestDetail() throws Exception {
        // given
        given(getInfoUpdateRequestDetailUsecase.getInfoUpdateRequestDetail(any()))
                .willReturn(detailResult(
                        InfoRequestStatus.REJECTED,
                        linkedInfoResult(),
                        new RejectionReason(REJECTION_REASON)
                ));

        // when & then
        mockMvc.perform(get(DETAIL_URL, REQUEST_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value(InfoRequestStatus.REJECTED.name()))
                .andExpect(jsonPath("$.data.info.id").value(INFO_ID))
                .andExpect(jsonPath("$.data.rejectionReason").value(REJECTION_REASON));
    }

    @Test
    @DisplayName("연결된 정보가 없는 정보 수정 요청을 조회하면 연결된 정보는 null로 반환한다")
    void getInfoUpdateRequestDetailWithoutLinkedInfo() throws Exception {
        // given
        given(getInfoUpdateRequestDetailUsecase.getInfoUpdateRequestDetail(any()))
                .willReturn(detailResult(InfoRequestStatus.PENDING, null, null));

        // when & then
        mockMvc.perform(get(DETAIL_URL, REQUEST_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(REQUEST_ID))
                .andExpect(jsonPath("$.data.info").value(nullValue()));
    }

    @Test
    @DisplayName("존재하지 않는 정보 수정 요청을 조회하면 정보 수정 요청 없음 에러를 응답한다")
    void getInfoUpdateRequestDetailWithNotExistingRequest() throws Exception {
        // given
        given(getInfoUpdateRequestDetailUsecase.getInfoUpdateRequestDetail(any()))
                .willThrow(new CustomException(BusinessInfoUpdateRequestErrorType.NOT_FOUND));

        // when & then
        mockMvc.perform(get(DETAIL_URL, REQUEST_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode")
                        .value(BusinessInfoUpdateRequestErrorType.NOT_FOUND.getErrorCode()))
                .andExpect(jsonPath("$.message")
                        .value(BusinessInfoUpdateRequestErrorType.NOT_FOUND.getMessage()));
    }

    @Test
    @DisplayName("요청한 회원 본인이 아닌 회원이 조회하면 접근 권한 없음 에러를 응답한다")
    void getInfoUpdateRequestDetailByOtherMember() throws Exception {
        // given
        given(getInfoUpdateRequestDetailUsecase.getInfoUpdateRequestDetail(any()))
                .willThrow(new CustomException(BusinessInfoUpdateRequestErrorType.FORBIDDEN));

        // when & then
        mockMvc.perform(get(DETAIL_URL, REQUEST_ID))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode")
                        .value(BusinessInfoUpdateRequestErrorType.FORBIDDEN.getErrorCode()))
                .andExpect(jsonPath("$.message")
                        .value(BusinessInfoUpdateRequestErrorType.FORBIDDEN.getMessage()));
    }

    @Test
    @DisplayName("정보 수정 요청 ID가 숫자가 아니면 잘못된 요청 에러를 응답하고 조회를 하지 않는다")
    void getInfoUpdateRequestDetailWithInvalidRequestId() throws Exception {
        // given & when
        mockMvc.perform(get(DETAIL_URL, "invalid-id"))
                .andExpect(status().isBadRequest());

        // then
        then(getInfoUpdateRequestDetailUsecase).should(never()).getInfoUpdateRequestDetail(any());
    }

    @Test
    @DisplayName("정보 수정 요청 제출 시 경로의 정보 ID, 인증된 회원 ID, 요청 본문으로 제출을 요청하고, 생성된 정보 수정 요청 ID를 반환한다")
    void submit() throws Exception {
        // given
        given(submitInfoUpdateRequestUsecase.submit(any()))
                .willReturn(new SubmitInfoUpdateRequestResult(new InfoUpdateRequestId(REQUEST_ID)));

        // when
        mockMvc.perform(post(SUBMIT_URL, INFO_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(CONTENT, CONTACT_EMAIL, OBSERVED_AT)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("정보 수정 요청 제출에 성공하였습니다."))
                .andExpect(jsonPath("$.data.infoUpdateRequestId").value(REQUEST_ID));

        // then
        then(submitInfoUpdateRequestUsecase).should().submit(commandCaptor.capture());
        SubmitInfoUpdateRequestCommand command = commandCaptor.getValue();
        assertEquals(new MemberId(MEMBER_ID), command.requesterId());
        assertEquals(new InfoId(INFO_ID), command.infoId());
        assertEquals(new InfoUpdateRequestContent(CONTENT), command.content());
        assertEquals(new Email(CONTACT_EMAIL), command.contactEmail());
        assertEquals(LocalDateTime.of(2026, 10, 1, 9, 0), command.observedAt());
    }

    @Test
    @DisplayName("요청 본문에 요청자 ID와 정보 ID가 포함되어도 무시하고 인증된 회원 ID와 경로의 정보 ID로 제출을 요청한다")
    void submitIgnoresRequesterIdAndInfoIdInBody() throws Exception {
        // given
        given(submitInfoUpdateRequestUsecase.submit(any()))
                .willReturn(new SubmitInfoUpdateRequestResult(new InfoUpdateRequestId(REQUEST_ID)));

        // when
        mockMvc.perform(post(SUBMIT_URL, INFO_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"requesterId": 999, "infoId": 999, "content": "%s", "contactEmail": "%s", "observedAt": "%s"}
                                """.formatted(CONTENT, CONTACT_EMAIL, OBSERVED_AT)))
                .andExpect(status().isCreated());

        // then
        then(submitInfoUpdateRequestUsecase).should().submit(commandCaptor.capture());
        assertEquals(new MemberId(MEMBER_ID), commandCaptor.getValue().requesterId());
        assertEquals(new InfoId(INFO_ID), commandCaptor.getValue().infoId());
    }

    @Test
    @DisplayName("대상 정보가 존재하지 않으면 정보 없음 에러를 응답한다")
    void submitWithNonExistentInfo() throws Exception {
        // given
        given(submitInfoUpdateRequestUsecase.submit(any()))
                .willThrow(new CustomException(BusinessInfoErrorType.NOT_FOUND));

        // when & then
        mockMvc.perform(post(SUBMIT_URL, INFO_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(CONTENT, CONTACT_EMAIL, OBSERVED_AT)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value(BusinessInfoErrorType.NOT_FOUND.getErrorCode()))
                .andExpect(jsonPath("$.message").value(BusinessInfoErrorType.NOT_FOUND.getMessage()));
    }

    @Test
    @DisplayName("정보 ID가 숫자가 아니면 잘못된 요청 에러를 응답하고 제출하지 않는다")
    void submitWithInvalidInfoId() throws Exception {
        // given & when
        mockMvc.perform(post(SUBMIT_URL, "invalid-id")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(CONTENT, CONTACT_EMAIL, OBSERVED_AT)))
                .andExpect(status().isBadRequest());

        // then
        then(submitInfoUpdateRequestUsecase).should(never()).submit(any());
    }

    @Test
    @DisplayName("필수 값이 모두 없으면 각 필드의 유효성 검증 에러를 응답하고 제출하지 않는다")
    void submitWithoutRequiredFields() throws Exception {
        // given & when
        mockMvc.perform(post(SUBMIT_URL, INFO_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.errorCode").value(CommonErrorType.VALIDATION_ERROR.getErrorCode()))
                .andExpect(jsonPath("$.errors.content").value("정보 수정 요청 내용은 비어있을 수 없습니다."))
                .andExpect(jsonPath("$.errors.contactEmail").exists())
                .andExpect(jsonPath("$.errors.observedAt").value("수정 정보 확인 시간은 비어있을 수 없습니다."));

        // then
        then(submitInfoUpdateRequestUsecase).should(never()).submit(any());
    }

    @Test
    @DisplayName("요청 내용이 공백이면 유효성 검증 에러를 응답하고 제출하지 않는다")
    void submitWithBlankContent() throws Exception {
        // given & when
        mockMvc.perform(post(SUBMIT_URL, INFO_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("   ", CONTACT_EMAIL, OBSERVED_AT)))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.errorCode").value(CommonErrorType.VALIDATION_ERROR.getErrorCode()))
                .andExpect(jsonPath("$.errors.content").value("정보 수정 요청 내용은 비어있을 수 없습니다."));

        // then
        then(submitInfoUpdateRequestUsecase).should(never()).submit(any());
    }

    @ParameterizedTest
    @ValueSource(strings = {"invalid-email", "user@localhost", "user@yu..ac.kr"})
    @DisplayName("연락용 이메일 형식이 Email VO의 형식과 다르면 유효성 검증 에러를 응답하고 제출하지 않는다")
    void submitWithInvalidContactEmail(String contactEmail) throws Exception {
        // given & when
        mockMvc.perform(post(SUBMIT_URL, INFO_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(CONTENT, contactEmail, OBSERVED_AT)))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.errorCode").value(CommonErrorType.VALIDATION_ERROR.getErrorCode()))
                .andExpect(jsonPath("$.errors.contactEmail").value("이메일 양식이 잘못되었습니다."));

        // then
        then(submitInfoUpdateRequestUsecase).should(never()).submit(any());
    }

    @Test
    @DisplayName("확인 시각 형식이 잘못되면 잘못된 요청 에러를 응답하고 제출하지 않는다")
    void submitWithInvalidObservedAt() throws Exception {
        // given & when
        mockMvc.perform(post(SUBMIT_URL, INFO_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(CONTENT, CONTACT_EMAIL, "2026/10/01 09:00")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value(CommonErrorType.BAD_REQUEST.getErrorCode()));

        // then
        then(submitInfoUpdateRequestUsecase).should(never()).submit(any());
    }

    private InfoUpdateRequestSummaryResult summaryResult(
            Long requestId,
            String title,
            InfoRequestStatus status,
            LocalDateTime requestedAt
    ) {
        return new InfoUpdateRequestSummaryResult(
                new InfoUpdateRequestId(requestId),
                InfoCategory.valueOf(INFO_CATEGORY),
                new InfoTitle(title),
                status,
                requestedAt
        );
    }

    private InfoUpdateRequestDetailResult detailResult(
            InfoRequestStatus status,
            LinkedInfoResult info,
            RejectionReason rejectionReason
    ) {
        return new InfoUpdateRequestDetailResult(
                new InfoUpdateRequestId(REQUEST_ID),
                new MemberId(MEMBER_ID),
                new InfoUpdateRequestContent(CONTENT),
                new Email(CONTACT_EMAIL),
                OBSERVED_AT_TIME,
                REQUESTED_AT,
                status,
                info,
                rejectionReason
        );
    }

    private LinkedInfoResult linkedInfoResult() {
        return new LinkedInfoResult(
                new InfoId(INFO_ID),
                InfoCategory.valueOf(INFO_CATEGORY),
                new InfoTitle(INFO_TITLE),
                INFO_UPDATED_AT
        );
    }

    private String body(String content, String contactEmail, String observedAt) {
        return """
                {"content": "%s", "contactEmail": "%s", "observedAt": "%s"}
                """.formatted(content, contactEmail, observedAt);
    }
}
