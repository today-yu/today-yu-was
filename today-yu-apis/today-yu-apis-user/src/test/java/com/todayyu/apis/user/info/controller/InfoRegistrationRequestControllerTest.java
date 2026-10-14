package com.todayyu.apis.user.info.controller;

import com.todayyu.apis.common.exception.GlobalExceptionHandler;
import com.todayyu.apis.common.security.principal.CustomUserDetails;
import com.todayyu.application.info.dto.command.SubmitInfoRegistrationRequestCommand;
import com.todayyu.application.info.dto.query.GetInfoRegistrationRequestDetailQuery;
import com.todayyu.application.info.dto.result.InfoRegistrationRequestDetailResult;
import com.todayyu.application.info.dto.result.LinkedInfoResult;
import com.todayyu.application.info.dto.result.SubmitInfoRegistrationRequestResult;
import com.todayyu.application.info.error.BusinessInfoRegistrationRequestErrorType;
import com.todayyu.application.info.usecase.GetInfoRegistrationRequestDetailUsecase;
import com.todayyu.application.info.usecase.SubmitInfoRegistrationRequestUsecase;
import com.todayyu.common.error.CommonErrorType;
import com.todayyu.common.exception.CustomException;
import com.todayyu.domain.common.vo.Email;
import com.todayyu.domain.info.vo.InfoCategory;
import com.todayyu.domain.info.vo.InfoId;
import com.todayyu.domain.info.vo.InfoRegistrationRequestContent;
import com.todayyu.domain.info.vo.InfoRegistrationRequestId;
import com.todayyu.domain.info.vo.InfoRequestStatus;
import com.todayyu.domain.info.vo.InfoTitle;
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

import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class InfoRegistrationRequestControllerTest {
    private static final String SUBMIT_URL = "/api/v1/infos/registration-requests";
    private static final String DETAIL_URL = "/api/v1/infos/registration-requests/{requestId}";
    private static final Long MEMBER_ID = 1L;
    private static final Long REQUEST_ID = 10L;
    private static final String CATEGORY = "ACADEMIC";
    private static final String CONTENT = "수강신청 일정을 등록해주세요.";
    private static final String CONTACT_EMAIL = "requester@example.com";
    private static final LocalDateTime REQUESTED_AT = LocalDateTime.of(2026, 10, 1, 9, 0, 30);
    private static final Long INFO_ID = 100L;
    private static final String INFO_TITLE = "수강신청 일정";
    private static final LocalDateTime INFO_UPDATED_AT = LocalDateTime.of(2026, 10, 5, 12, 0, 30);
    private static final String REJECTION_REASON = "사실 관계를 확인할 수 없습니다.";

    @Mock
    private GetInfoRegistrationRequestDetailUsecase getInfoRegistrationRequestDetailUsecase;

    @Mock
    private SubmitInfoRegistrationRequestUsecase submitInfoRegistrationRequestUsecase;

    @InjectMocks
    private InfoRegistrationRequestController infoRegistrationRequestController;

    @Captor
    private ArgumentCaptor<SubmitInfoRegistrationRequestCommand> commandCaptor;

    @Captor
    private ArgumentCaptor<GetInfoRegistrationRequestDetailQuery> detailQueryCaptor;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(infoRegistrationRequestController)
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
    @DisplayName("정보 등록 요청 상세 조회 요청 시 요청 ID와 인증된 회원 ID로 조회를 요청하고, 상세 내용을 원시값으로 변환해 반환한다")
    void getInfoRegistrationRequestDetail() throws Exception {
        // given
        given(getInfoRegistrationRequestDetailUsecase.getInfoRegistrationRequestDetail(any()))
                .willReturn(detailResult(InfoRequestStatus.PENDING, null, null));

        // when
        mockMvc.perform(get(DETAIL_URL, REQUEST_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("정보 등록 요청 상세 조회에 성공하였습니다."))
                .andExpect(jsonPath("$.data.id").value(REQUEST_ID))
                .andExpect(jsonPath("$.data.requesterId").value(MEMBER_ID))
                .andExpect(jsonPath("$.data.category").value(CATEGORY))
                .andExpect(jsonPath("$.data.content").value(CONTENT))
                .andExpect(jsonPath("$.data.contactEmail").value(CONTACT_EMAIL))
                .andExpect(jsonPath("$.data.requestedAt").value(REQUESTED_AT.toString()))
                .andExpect(jsonPath("$.data.status").value(InfoRequestStatus.PENDING.name()))
                .andExpect(jsonPath("$.data.info").value(nullValue()))
                .andExpect(jsonPath("$.data.rejectionReason").value(nullValue()));

        // then
        then(getInfoRegistrationRequestDetailUsecase).should()
                .getInfoRegistrationRequestDetail(detailQueryCaptor.capture());
        assertEquals(new InfoRegistrationRequestId(REQUEST_ID), detailQueryCaptor.getValue().id());
        assertEquals(new MemberId(MEMBER_ID), detailQueryCaptor.getValue().memberId());
    }

    @Test
    @DisplayName("반영(ACCEPTED)된 정보 등록 요청을 조회하면 연결된 정보의 ID, 카테고리, 제목, 최근 갱신 일시를 반환하고 반려 사유는 null이다")
    void getAcceptedInfoRegistrationRequestDetail() throws Exception {
        // given
        given(getInfoRegistrationRequestDetailUsecase.getInfoRegistrationRequestDetail(any()))
                .willReturn(detailResult(InfoRequestStatus.ACCEPTED, linkedInfoResult(), null));

        // when & then
        mockMvc.perform(get(DETAIL_URL, REQUEST_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value(InfoRequestStatus.ACCEPTED.name()))
                .andExpect(jsonPath("$.data.info.id").value(INFO_ID))
                .andExpect(jsonPath("$.data.info.category").value(CATEGORY))
                .andExpect(jsonPath("$.data.info.title").value(INFO_TITLE))
                .andExpect(jsonPath("$.data.info.updatedAt").value(INFO_UPDATED_AT.toString()))
                .andExpect(jsonPath("$.data.rejectionReason").value(nullValue()));
    }

    @Test
    @DisplayName("반려(REJECTED)된 정보 등록 요청을 조회하면 반려 사유를 반환하고 연결된 정보는 null이다")
    void getRejectedInfoRegistrationRequestDetail() throws Exception {
        // given
        given(getInfoRegistrationRequestDetailUsecase.getInfoRegistrationRequestDetail(any()))
                .willReturn(detailResult(InfoRequestStatus.REJECTED, null, new RejectionReason(REJECTION_REASON)));

        // when & then
        mockMvc.perform(get(DETAIL_URL, REQUEST_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value(InfoRequestStatus.REJECTED.name()))
                .andExpect(jsonPath("$.data.info").value(nullValue()))
                .andExpect(jsonPath("$.data.rejectionReason").value(REJECTION_REASON));
    }

    @Test
    @DisplayName("존재하지 않는 정보 등록 요청을 조회하면 정보 등록 요청 없음 에러를 응답한다")
    void getInfoRegistrationRequestDetailWithNotExistingRequest() throws Exception {
        // given
        given(getInfoRegistrationRequestDetailUsecase.getInfoRegistrationRequestDetail(any()))
                .willThrow(new CustomException(BusinessInfoRegistrationRequestErrorType.NOT_FOUND));

        // when & then
        mockMvc.perform(get(DETAIL_URL, REQUEST_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode")
                        .value(BusinessInfoRegistrationRequestErrorType.NOT_FOUND.getErrorCode()))
                .andExpect(jsonPath("$.message")
                        .value(BusinessInfoRegistrationRequestErrorType.NOT_FOUND.getMessage()));
    }

    @Test
    @DisplayName("요청한 회원 본인이 아닌 회원이 조회하면 접근 권한 없음 에러를 응답한다")
    void getInfoRegistrationRequestDetailByOtherMember() throws Exception {
        // given
        given(getInfoRegistrationRequestDetailUsecase.getInfoRegistrationRequestDetail(any()))
                .willThrow(new CustomException(BusinessInfoRegistrationRequestErrorType.FORBIDDEN));

        // when & then
        mockMvc.perform(get(DETAIL_URL, REQUEST_ID))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode")
                        .value(BusinessInfoRegistrationRequestErrorType.FORBIDDEN.getErrorCode()))
                .andExpect(jsonPath("$.message")
                        .value(BusinessInfoRegistrationRequestErrorType.FORBIDDEN.getMessage()));
    }

    @Test
    @DisplayName("정보 등록 요청 ID가 숫자가 아니면 잘못된 요청 에러를 응답하고 조회를 하지 않는다")
    void getInfoRegistrationRequestDetailWithInvalidRequestId() throws Exception {
        // given & when
        mockMvc.perform(get(DETAIL_URL, "invalid-id"))
                .andExpect(status().isBadRequest());

        // then
        then(getInfoRegistrationRequestDetailUsecase).should(never()).getInfoRegistrationRequestDetail(any());
    }

    @Test
    @DisplayName("정보 등록 요청 제출 시 인증된 회원 ID와 요청 본문으로 제출을 요청하고, 생성된 정보 등록 요청 ID를 반환한다")
    void submit() throws Exception {
        // given
        given(submitInfoRegistrationRequestUsecase.submit(any()))
                .willReturn(new SubmitInfoRegistrationRequestResult(new InfoRegistrationRequestId(REQUEST_ID)));

        // when
        mockMvc.perform(post(SUBMIT_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(CATEGORY, CONTENT, CONTACT_EMAIL)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("정보 등록 요청 제출에 성공하였습니다."))
                .andExpect(jsonPath("$.data.infoRegistrationRequestId").value(REQUEST_ID));

        // then
        then(submitInfoRegistrationRequestUsecase).should().submit(commandCaptor.capture());
        SubmitInfoRegistrationRequestCommand command = commandCaptor.getValue();
        assertEquals(new MemberId(MEMBER_ID), command.requesterId());
        assertEquals(InfoCategory.ACADEMIC, command.category());
        assertEquals(new InfoRegistrationRequestContent(CONTENT), command.content());
        assertEquals(new Email(CONTACT_EMAIL), command.contactEmail());
    }

    @Test
    @DisplayName("요청 본문에 요청자 ID가 포함되어도 무시하고 인증된 회원 ID로 제출을 요청한다")
    void submitIgnoresRequesterIdInBody() throws Exception {
        // given
        given(submitInfoRegistrationRequestUsecase.submit(any()))
                .willReturn(new SubmitInfoRegistrationRequestResult(new InfoRegistrationRequestId(REQUEST_ID)));

        // when
        mockMvc.perform(post(SUBMIT_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"requesterId": 999, "category": "%s", "content": "%s", "contactEmail": "%s"}
                                """.formatted(CATEGORY, CONTENT, CONTACT_EMAIL)))
                .andExpect(status().isCreated());

        // then
        then(submitInfoRegistrationRequestUsecase).should().submit(commandCaptor.capture());
        assertEquals(new MemberId(MEMBER_ID), commandCaptor.getValue().requesterId());
    }

    @Test
    @DisplayName("필수 값이 모두 없으면 각 필드의 유효성 검증 에러를 응답하고 제출하지 않는다")
    void submitWithoutRequiredFields() throws Exception {
        // given & when
        mockMvc.perform(post(SUBMIT_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.errorCode").value(CommonErrorType.VALIDATION_ERROR.getErrorCode()))
                .andExpect(jsonPath("$.errors.category").value("정보 카테고리는 비어있을 수 없습니다."))
                .andExpect(jsonPath("$.errors.content").value("정보 등록 요청 내용은 비어있을 수 없습니다."))
                .andExpect(jsonPath("$.errors.contactEmail").exists());

        // then
        then(submitInfoRegistrationRequestUsecase).should(never()).submit(any());
    }

    @Test
    @DisplayName("요청 내용이 공백이면 유효성 검증 에러를 응답하고 제출하지 않는다")
    void submitWithBlankContent() throws Exception {
        // given & when
        mockMvc.perform(post(SUBMIT_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(CATEGORY, "   ", CONTACT_EMAIL)))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.errorCode").value(CommonErrorType.VALIDATION_ERROR.getErrorCode()))
                .andExpect(jsonPath("$.errors.content").value("정보 등록 요청 내용은 비어있을 수 없습니다."));

        // then
        then(submitInfoRegistrationRequestUsecase).should(never()).submit(any());
    }

    @ParameterizedTest
    @ValueSource(strings = {"invalid-email", "user@localhost", "user@yu..ac.kr"})
    @DisplayName("연락용 이메일 형식이 Email VO의 형식과 다르면 유효성 검증 에러를 응답하고 제출하지 않는다")
    void submitWithInvalidContactEmail(String contactEmail) throws Exception {
        // given & when
        mockMvc.perform(post(SUBMIT_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(CATEGORY, CONTENT, contactEmail)))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.errorCode").value(CommonErrorType.VALIDATION_ERROR.getErrorCode()))
                .andExpect(jsonPath("$.errors.contactEmail").value("이메일 양식이 잘못되었습니다."));

        // then
        then(submitInfoRegistrationRequestUsecase).should(never()).submit(any());
    }

    @Test
    @DisplayName("존재하지 않는 카테고리로 요청하면 잘못된 요청 에러를 응답하고 제출하지 않는다")
    void submitWithInvalidCategory() throws Exception {
        // given & when
        mockMvc.perform(post(SUBMIT_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("INVALID", CONTENT, CONTACT_EMAIL)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value(CommonErrorType.BAD_REQUEST.getErrorCode()));

        // then
        then(submitInfoRegistrationRequestUsecase).should(never()).submit(any());
    }

    private InfoRegistrationRequestDetailResult detailResult(
            InfoRequestStatus status,
            LinkedInfoResult info,
            RejectionReason rejectionReason
    ) {
        return new InfoRegistrationRequestDetailResult(
                new InfoRegistrationRequestId(REQUEST_ID),
                new MemberId(MEMBER_ID),
                InfoCategory.valueOf(CATEGORY),
                new InfoRegistrationRequestContent(CONTENT),
                new Email(CONTACT_EMAIL),
                REQUESTED_AT,
                status,
                info,
                rejectionReason
        );
    }

    private LinkedInfoResult linkedInfoResult() {
        return new LinkedInfoResult(
                new InfoId(INFO_ID),
                InfoCategory.valueOf(CATEGORY),
                new InfoTitle(INFO_TITLE),
                INFO_UPDATED_AT
        );
    }

    private String body(String category, String content, String contactEmail) {
        return """
                {"category": "%s", "content": "%s", "contactEmail": "%s"}
                """.formatted(category, content, contactEmail);
    }
}
