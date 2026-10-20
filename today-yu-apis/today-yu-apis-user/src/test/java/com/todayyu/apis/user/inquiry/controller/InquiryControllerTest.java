package com.todayyu.apis.user.inquiry.controller;

import com.todayyu.apis.common.exception.GlobalExceptionHandler;
import com.todayyu.apis.common.security.principal.CustomUserDetails;
import com.todayyu.application.common.dto.CursorResult;
import com.todayyu.application.inquiry.dto.InquiryCursor;
import com.todayyu.application.inquiry.dto.command.DeleteInquiryCommand;
import com.todayyu.application.inquiry.dto.command.SubmitInquiryCommand;
import com.todayyu.application.inquiry.dto.command.UpdateInquiryCommand;
import com.todayyu.application.inquiry.dto.query.GetInquiriesQuery;
import com.todayyu.application.inquiry.dto.query.GetInquiryDetailQuery;
import com.todayyu.application.inquiry.dto.result.InquiryDetailResult;
import com.todayyu.application.inquiry.dto.result.InquirySummaryResult;
import com.todayyu.application.inquiry.dto.result.SubmitInquiryResult;
import com.todayyu.application.inquiry.error.BusinessInquiryErrorType;
import com.todayyu.application.inquiry.usecase.DeleteInquiryUsecase;
import com.todayyu.application.inquiry.usecase.GetInquiriesUsecase;
import com.todayyu.application.inquiry.usecase.GetInquiryDetailUsecase;
import com.todayyu.application.inquiry.usecase.SubmitInquiryUsecase;
import com.todayyu.application.inquiry.usecase.UpdateInquiryUsecase;
import com.todayyu.common.error.CommonErrorType;
import com.todayyu.common.exception.CustomException;
import com.todayyu.domain.common.vo.Email;
import com.todayyu.domain.inquiry.error.InquiryErrorType;
import com.todayyu.domain.inquiry.vo.InquiryContent;
import com.todayyu.domain.inquiry.vo.InquiryId;
import com.todayyu.domain.inquiry.vo.InquiryStatus;
import com.todayyu.domain.inquiry.vo.InquiryTitle;
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
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class InquiryControllerTest {
    private static final String LIST_URL = "/api/v1/inquiries";
    private static final String SUBMIT_URL = "/api/v1/inquiries";
    private static final String DETAIL_URL = "/api/v1/inquiries/{inquiryId}";
    private static final String UPDATE_URL = "/api/v1/inquiries/{inquiryId}";
    private static final String DELETE_URL = "/api/v1/inquiries/{inquiryId}";
    private static final Long MEMBER_ID = 1L;
    private static final Long INQUIRY_ID = 10L;
    private static final Long SECOND_INQUIRY_ID = 11L;
    private static final Long CURSOR_INQUIRY_ID = 12L;
    private static final String CONTACT_EMAIL = "member@example.com";
    private static final String TITLE = "문의 제목";
    private static final String SECOND_TITLE = "두 번째 문의 제목";
    private static final String CONTENT = "문의 내용";
    private static final LocalDateTime CREATED_AT = LocalDateTime.of(2026, 10, 1, 9, 0, 30);
    private static final LocalDateTime UPDATED_AT = LocalDateTime.of(2026, 10, 2, 9, 0, 30);
    private static final LocalDateTime ANSWERED_AT = LocalDateTime.of(2026, 10, 3, 9, 0, 30);

    @Mock
    private GetInquiriesUsecase getInquiriesUsecase;

    @Mock
    private GetInquiryDetailUsecase getInquiryDetailUsecase;

    @Mock
    private SubmitInquiryUsecase submitInquiryUsecase;

    @Mock
    private UpdateInquiryUsecase updateInquiryUsecase;

    @Mock
    private DeleteInquiryUsecase deleteInquiryUsecase;

    @InjectMocks
    private InquiryController inquiryController;

    @Captor
    private ArgumentCaptor<SubmitInquiryCommand> commandCaptor;

    @Captor
    private ArgumentCaptor<GetInquiryDetailQuery> detailQueryCaptor;

    @Captor
    private ArgumentCaptor<GetInquiriesQuery> inquiriesQueryCaptor;

    @Captor
    private ArgumentCaptor<UpdateInquiryCommand> updateCommandCaptor;

    @Captor
    private ArgumentCaptor<DeleteInquiryCommand> deleteCommandCaptor;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(inquiryController)
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
    @DisplayName("문의 목록 조회 요청 시 인증된 회원 ID와 커서·조회 크기로 조회를 요청하고, 목록과 다음 커서를 원시값으로 변환해 반환한다")
    void getInquiries() throws Exception {
        // given
        given(getInquiriesUsecase.getInquiries(any())).willReturn(new CursorResult<>(
                List.of(
                        summaryResult(SECOND_INQUIRY_ID, InquiryStatus.ANSWERED, SECOND_TITLE),
                        summaryResult(INQUIRY_ID, InquiryStatus.WAITING, TITLE)
                ),
                true,
                new InquiryCursor(new InquiryId(INQUIRY_ID))
        ));

        // when
        mockMvc.perform(get(LIST_URL)
                        .param("cursorId", String.valueOf(CURSOR_INQUIRY_ID))
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("문의 목록 조회에 성공하였습니다."))
                .andExpect(jsonPath("$.data.contents.length()").value(2))
                .andExpect(jsonPath("$.data.contents[0].id").value(SECOND_INQUIRY_ID))
                .andExpect(jsonPath("$.data.contents[0].status").value(InquiryStatus.ANSWERED.name()))
                .andExpect(jsonPath("$.data.contents[0].title").value(SECOND_TITLE))
                .andExpect(jsonPath("$.data.contents[0].createdAt").value(CREATED_AT.toString()))
                .andExpect(jsonPath("$.data.contents[0].updatedAt").value(UPDATED_AT.toString()))
                .andExpect(jsonPath("$.data.contents[1].id").value(INQUIRY_ID))
                .andExpect(jsonPath("$.data.contents[1].status").value(InquiryStatus.WAITING.name()))
                .andExpect(jsonPath("$.data.contents[1].title").value(TITLE))
                .andExpect(jsonPath("$.data.hasNext").value(true))
                .andExpect(jsonPath("$.data.nextCursor.id").value(INQUIRY_ID));

        // then
        then(getInquiriesUsecase).should().getInquiries(inquiriesQueryCaptor.capture());
        GetInquiriesQuery query = inquiriesQueryCaptor.getValue();
        assertEquals(new MemberId(MEMBER_ID), query.memberId());
        assertEquals(new InquiryCursor(new InquiryId(CURSOR_INQUIRY_ID)), query.cursor());
        assertEquals(2, query.size());
    }

    @Test
    @DisplayName("첫 요청(커서·조회 크기 없음)이면 커서 없이 기본 조회 크기로 조회를 요청하고, 다음 데이터가 없으면 다음 커서는 null이다")
    void getInquiriesWithFirstRequest() throws Exception {
        // given
        given(getInquiriesUsecase.getInquiries(any())).willReturn(new CursorResult<>(List.of(), false, null));

        // when
        mockMvc.perform(get(LIST_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.contents").isArray())
                .andExpect(jsonPath("$.data.contents.length()").value(0))
                .andExpect(jsonPath("$.data.hasNext").value(false))
                .andExpect(jsonPath("$.data.nextCursor").value(nullValue()));

        // then
        then(getInquiriesUsecase).should().getInquiries(inquiriesQueryCaptor.capture());
        GetInquiriesQuery query = inquiriesQueryCaptor.getValue();
        assertEquals(new MemberId(MEMBER_ID), query.memberId());
        assertNull(query.cursor());
        assertEquals(GetInquiriesQuery.DEFAULT_SIZE, query.size());
    }

    @ParameterizedTest
    @ValueSource(ints = {GetInquiriesQuery.MIN_SIZE - 1, GetInquiriesQuery.MAX_SIZE + 1})
    @DisplayName("조회 크기가 허용 범위를 벗어나면 유효성 검증 에러를 응답하고 조회를 하지 않는다")
    void getInquiriesWithOutOfRangeSize(int size) throws Exception {
        // given & when
        mockMvc.perform(get(LIST_URL)
                        .param("size", String.valueOf(size)))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.errorCode").value(CommonErrorType.VALIDATION_ERROR.getErrorCode()))
                .andExpect(jsonPath("$.errors.size").exists());

        // then
        then(getInquiriesUsecase).should(never()).getInquiries(any());
    }

    @Test
    @DisplayName("문의 상세 조회 요청 시 문의 ID와 인증된 회원 ID로 조회를 요청하고, 상세 내용을 원시값으로 변환해 반환한다")
    void getInquiryDetail() throws Exception {
        // given
        given(getInquiryDetailUsecase.getInquiryDetail(any()))
                .willReturn(detailResult(InquiryStatus.WAITING, null));

        // when
        mockMvc.perform(get(DETAIL_URL, INQUIRY_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("문의 상세 조회에 성공하였습니다."))
                .andExpect(jsonPath("$.data.id").value(INQUIRY_ID))
                .andExpect(jsonPath("$.data.memberId").value(MEMBER_ID))
                .andExpect(jsonPath("$.data.contactEmail").value(CONTACT_EMAIL))
                .andExpect(jsonPath("$.data.status").value(InquiryStatus.WAITING.name()))
                .andExpect(jsonPath("$.data.title").value(TITLE))
                .andExpect(jsonPath("$.data.content").value(CONTENT))
                .andExpect(jsonPath("$.data.createdAt").value(CREATED_AT.toString()))
                .andExpect(jsonPath("$.data.updatedAt").value(UPDATED_AT.toString()))
                .andExpect(jsonPath("$.data.answeredAt").value(nullValue()));

        // then
        then(getInquiryDetailUsecase).should().getInquiryDetail(detailQueryCaptor.capture());
        assertEquals(new InquiryId(INQUIRY_ID), detailQueryCaptor.getValue().inquiryId());
        assertEquals(new MemberId(MEMBER_ID), detailQueryCaptor.getValue().memberId());
    }

    @Test
    @DisplayName("답변 완료(ANSWERED)된 문의를 조회하면 답변 시각을 함께 반환한다")
    void getAnsweredInquiryDetail() throws Exception {
        // given
        given(getInquiryDetailUsecase.getInquiryDetail(any()))
                .willReturn(detailResult(InquiryStatus.ANSWERED, ANSWERED_AT));

        // when & then
        mockMvc.perform(get(DETAIL_URL, INQUIRY_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value(InquiryStatus.ANSWERED.name()))
                .andExpect(jsonPath("$.data.answeredAt").value(ANSWERED_AT.toString()));
    }

    @Test
    @DisplayName("존재하지 않는 문의를 조회하면 문의 없음 에러를 응답한다")
    void getInquiryDetailWithNotExistingInquiry() throws Exception {
        // given
        given(getInquiryDetailUsecase.getInquiryDetail(any()))
                .willThrow(new CustomException(BusinessInquiryErrorType.NOT_FOUND));

        // when & then
        mockMvc.perform(get(DETAIL_URL, INQUIRY_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value(BusinessInquiryErrorType.NOT_FOUND.getErrorCode()))
                .andExpect(jsonPath("$.message").value(BusinessInquiryErrorType.NOT_FOUND.getMessage()));
    }

    @Test
    @DisplayName("작성한 회원 본인이 아닌 회원이 조회하면 접근 권한 없음 에러를 응답한다")
    void getInquiryDetailByOtherMember() throws Exception {
        // given
        given(getInquiryDetailUsecase.getInquiryDetail(any()))
                .willThrow(new CustomException(BusinessInquiryErrorType.FORBIDDEN));

        // when & then
        mockMvc.perform(get(DETAIL_URL, INQUIRY_ID))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value(BusinessInquiryErrorType.FORBIDDEN.getErrorCode()))
                .andExpect(jsonPath("$.message").value(BusinessInquiryErrorType.FORBIDDEN.getMessage()));
    }

    @Test
    @DisplayName("문의 작성 시 인증된 회원 ID와 요청 본문으로 작성을 요청하고, 생성된 문의 ID를 반환한다")
    void submitInquiry() throws Exception {
        // given
        given(submitInquiryUsecase.submit(any()))
                .willReturn(new SubmitInquiryResult(new InquiryId(INQUIRY_ID)));

        // when
        mockMvc.perform(post(SUBMIT_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(CONTACT_EMAIL, TITLE, CONTENT)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("문의 작성에 성공하였습니다."))
                .andExpect(jsonPath("$.data.inquiryId").value(INQUIRY_ID));

        // then
        then(submitInquiryUsecase).should().submit(commandCaptor.capture());
        SubmitInquiryCommand command = commandCaptor.getValue();
        assertEquals(new MemberId(MEMBER_ID), command.memberId());
        assertEquals(new Email(CONTACT_EMAIL), command.contactEmail());
        assertEquals(new InquiryTitle(TITLE), command.title());
        assertEquals(new InquiryContent(CONTENT), command.content());
    }

    @Test
    @DisplayName("요청 본문에 회원 ID가 포함되어도 무시하고 인증된 회원 ID로 작성을 요청한다")
    void submitInquiryIgnoresMemberIdInBody() throws Exception {
        // given
        given(submitInquiryUsecase.submit(any()))
                .willReturn(new SubmitInquiryResult(new InquiryId(INQUIRY_ID)));

        // when
        mockMvc.perform(post(SUBMIT_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"memberId": 999, "contactEmail": "%s", "title": "%s", "content": "%s"}
                                """.formatted(CONTACT_EMAIL, TITLE, CONTENT)))
                .andExpect(status().isCreated());

        // then
        then(submitInquiryUsecase).should().submit(commandCaptor.capture());
        assertEquals(new MemberId(MEMBER_ID), commandCaptor.getValue().memberId());
    }

    @Test
    @DisplayName("필수 값이 모두 없으면 각 필드의 유효성 검증 에러를 응답하고 작성하지 않는다")
    void submitInquiryWithoutRequiredFields() throws Exception {
        // given & when
        mockMvc.perform(post(SUBMIT_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.errorCode").value(CommonErrorType.VALIDATION_ERROR.getErrorCode()))
                .andExpect(jsonPath("$.errors.contactEmail").value("이메일은 비어있을 수 없습니다."))
                .andExpect(jsonPath("$.errors.title").value("제목은 비어있을 수 없습니다."))
                .andExpect(jsonPath("$.errors.content").value("내용은 비어있을 수 없습니다."));

        // then
        then(submitInquiryUsecase).should(never()).submit(any());
    }

    @Test
    @DisplayName("문의 제목이 공백이면 유효성 검증 에러를 응답하고 작성하지 않는다")
    void submitInquiryWithBlankTitle() throws Exception {
        // given & when
        mockMvc.perform(post(SUBMIT_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(CONTACT_EMAIL, "   ", CONTENT)))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.errorCode").value(CommonErrorType.VALIDATION_ERROR.getErrorCode()))
                .andExpect(jsonPath("$.errors.title").value("제목은 비어있을 수 없습니다."));

        // then
        then(submitInquiryUsecase).should(never()).submit(any());
    }

    @Test
    @DisplayName("문의 내용이 공백이면 유효성 검증 에러를 응답하고 작성하지 않는다")
    void submitInquiryWithBlankContent() throws Exception {
        // given & when
        mockMvc.perform(post(SUBMIT_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(CONTACT_EMAIL, TITLE, "   ")))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.errorCode").value(CommonErrorType.VALIDATION_ERROR.getErrorCode()))
                .andExpect(jsonPath("$.errors.content").value("내용은 비어있을 수 없습니다."));

        // then
        then(submitInquiryUsecase).should(never()).submit(any());
    }

    @ParameterizedTest
    @ValueSource(strings = {"invalid-email", "user@localhost", "user@yu..ac.kr"})
    @DisplayName("연락용 이메일 형식이 Email VO의 형식과 다르면 유효성 검증 에러를 응답하고 작성하지 않는다")
    void submitInquiryWithInvalidContactEmail(String contactEmail) throws Exception {
        // given & when
        mockMvc.perform(post(SUBMIT_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(contactEmail, TITLE, CONTENT)))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.errorCode").value(CommonErrorType.VALIDATION_ERROR.getErrorCode()))
                .andExpect(jsonPath("$.errors.contactEmail").value("이메일 양식이 잘못되었습니다."));

        // then
        then(submitInquiryUsecase).should(never()).submit(any());
    }

    @Test
    @DisplayName("문의 수정 요청 시 문의 ID와 인증된 회원 ID, 요청 본문으로 수정을 요청하고, 성공 메시지를 반환한다")
    void updateInquiry() throws Exception {
        // when
        mockMvc.perform(put(UPDATE_URL, INQUIRY_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(CONTACT_EMAIL, TITLE, CONTENT)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("문의 수정에 성공하였습니다."))
                .andExpect(jsonPath("$.data").value(nullValue()));

        // then
        then(updateInquiryUsecase).should().update(updateCommandCaptor.capture());
        UpdateInquiryCommand command = updateCommandCaptor.getValue();
        assertEquals(new InquiryId(INQUIRY_ID), command.id());
        assertEquals(new MemberId(MEMBER_ID), command.memberId());
        assertEquals(new Email(CONTACT_EMAIL), command.contactEmail());
        assertEquals(new InquiryTitle(TITLE), command.title());
        assertEquals(new InquiryContent(CONTENT), command.content());
    }

    @Test
    @DisplayName("요청 본문에 회원 ID가 포함되어도 무시하고 인증된 회원 ID로 수정을 요청한다")
    void updateInquiryIgnoresMemberIdInBody() throws Exception {
        // when
        mockMvc.perform(put(UPDATE_URL, INQUIRY_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"memberId": 999, "contactEmail": "%s", "title": "%s", "content": "%s"}
                                """.formatted(CONTACT_EMAIL, TITLE, CONTENT)))
                .andExpect(status().isOk());

        // then
        then(updateInquiryUsecase).should().update(updateCommandCaptor.capture());
        assertEquals(new MemberId(MEMBER_ID), updateCommandCaptor.getValue().memberId());
    }

    @Test
    @DisplayName("수정 요청에 필수 값이 모두 없으면 각 필드의 유효성 검증 에러를 응답하고 수정하지 않는다")
    void updateInquiryWithoutRequiredFields() throws Exception {
        // given & when
        mockMvc.perform(put(UPDATE_URL, INQUIRY_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.errorCode").value(CommonErrorType.VALIDATION_ERROR.getErrorCode()))
                .andExpect(jsonPath("$.errors.contactEmail").value("이메일은 비어있을 수 없습니다."))
                .andExpect(jsonPath("$.errors.title").value("제목은 비어있을 수 없습니다."))
                .andExpect(jsonPath("$.errors.content").value("내용은 비어있을 수 없습니다."));

        // then
        then(updateInquiryUsecase).should(never()).update(any());
    }

    @ParameterizedTest
    @ValueSource(strings = {"invalid-email", "user@localhost", "user@yu..ac.kr"})
    @DisplayName("수정할 연락용 이메일 형식이 Email VO의 형식과 다르면 유효성 검증 에러를 응답하고 수정하지 않는다")
    void updateInquiryWithInvalidContactEmail(String contactEmail) throws Exception {
        // given & when
        mockMvc.perform(put(UPDATE_URL, INQUIRY_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(contactEmail, TITLE, CONTENT)))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.errorCode").value(CommonErrorType.VALIDATION_ERROR.getErrorCode()))
                .andExpect(jsonPath("$.errors.contactEmail").value("이메일 양식이 잘못되었습니다."));

        // then
        then(updateInquiryUsecase).should(never()).update(any());
    }

    @Test
    @DisplayName("존재하지 않는 문의를 수정하면 문의 없음 에러를 응답한다")
    void updateInquiryWithNotExistingInquiry() throws Exception {
        // given
        willThrow(new CustomException(BusinessInquiryErrorType.NOT_FOUND))
                .given(updateInquiryUsecase).update(any());

        // when & then
        mockMvc.perform(put(UPDATE_URL, INQUIRY_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(CONTACT_EMAIL, TITLE, CONTENT)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value(BusinessInquiryErrorType.NOT_FOUND.getErrorCode()))
                .andExpect(jsonPath("$.message").value(BusinessInquiryErrorType.NOT_FOUND.getMessage()));
    }

    @Test
    @DisplayName("작성한 회원 본인이 아닌 회원이 수정하면 접근 권한 없음 에러를 응답한다")
    void updateInquiryByOtherMember() throws Exception {
        // given
        willThrow(new CustomException(BusinessInquiryErrorType.FORBIDDEN))
                .given(updateInquiryUsecase).update(any());

        // when & then
        mockMvc.perform(put(UPDATE_URL, INQUIRY_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(CONTACT_EMAIL, TITLE, CONTENT)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value(BusinessInquiryErrorType.FORBIDDEN.getErrorCode()))
                .andExpect(jsonPath("$.message").value(BusinessInquiryErrorType.FORBIDDEN.getMessage()));
    }

    @Test
    @DisplayName("답변 대기 중 상태가 아닌 문의를 수정하면 수정 불가 에러를 응답한다")
    void updateAnsweredInquiry() throws Exception {
        // given
        willThrow(new CustomException(InquiryErrorType.FAIL_UPDATE))
                .given(updateInquiryUsecase).update(any());

        // when & then
        mockMvc.perform(put(UPDATE_URL, INQUIRY_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(CONTACT_EMAIL, TITLE, CONTENT)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value(InquiryErrorType.FAIL_UPDATE.getErrorCode()))
                .andExpect(jsonPath("$.message").value(InquiryErrorType.FAIL_UPDATE.getMessage()));
    }

    @Test
    @DisplayName("문의 삭제 요청 시 문의 ID와 인증된 회원 ID로 삭제를 요청하고, 성공 메시지를 반환한다")
    void deleteInquiry() throws Exception {
        // when
        mockMvc.perform(delete(DELETE_URL, INQUIRY_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("문의 삭제에 성공하였습니다."));

        // then
        then(deleteInquiryUsecase).should().delete(deleteCommandCaptor.capture());
        assertEquals(new InquiryId(INQUIRY_ID), deleteCommandCaptor.getValue().id());
        assertEquals(new MemberId(MEMBER_ID), deleteCommandCaptor.getValue().memberId());
    }

    @Test
    @DisplayName("존재하지 않는 문의를 삭제하면 문의 없음 에러를 응답한다")
    void deleteInquiryWithNotExistingInquiry() throws Exception {
        // given
        willThrow(new CustomException(BusinessInquiryErrorType.NOT_FOUND))
                .given(deleteInquiryUsecase).delete(any());

        // when & then
        mockMvc.perform(delete(DELETE_URL, INQUIRY_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value(BusinessInquiryErrorType.NOT_FOUND.getErrorCode()))
                .andExpect(jsonPath("$.message").value(BusinessInquiryErrorType.NOT_FOUND.getMessage()));
    }

    @Test
    @DisplayName("작성한 회원 본인이 아닌 회원이 삭제하면 접근 권한 없음 에러를 응답한다")
    void deleteInquiryByOtherMember() throws Exception {
        // given
        willThrow(new CustomException(BusinessInquiryErrorType.FORBIDDEN))
                .given(deleteInquiryUsecase).delete(any());

        // when & then
        mockMvc.perform(delete(DELETE_URL, INQUIRY_ID))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value(BusinessInquiryErrorType.FORBIDDEN.getErrorCode()))
                .andExpect(jsonPath("$.message").value(BusinessInquiryErrorType.FORBIDDEN.getMessage()));
    }

    @Test
    @DisplayName("답변 대기 중 상태가 아닌 문의를 삭제하면 삭제 불가 에러를 응답한다")
    void deleteAnsweredInquiry() throws Exception {
        // given
        willThrow(new CustomException(BusinessInquiryErrorType.FAIL_DELETE))
                .given(deleteInquiryUsecase).delete(any());

        // when & then
        mockMvc.perform(delete(DELETE_URL, INQUIRY_ID))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value(BusinessInquiryErrorType.FAIL_DELETE.getErrorCode()))
                .andExpect(jsonPath("$.message").value(BusinessInquiryErrorType.FAIL_DELETE.getMessage()));
    }

    private InquirySummaryResult summaryResult(Long inquiryId, InquiryStatus status, String title) {
        return new InquirySummaryResult(
                new InquiryId(inquiryId),
                status,
                new InquiryTitle(title),
                CREATED_AT,
                UPDATED_AT
        );
    }

    private InquiryDetailResult detailResult(InquiryStatus status, LocalDateTime answeredAt) {
        return new InquiryDetailResult(
                new InquiryId(INQUIRY_ID),
                new MemberId(MEMBER_ID),
                new Email(CONTACT_EMAIL),
                status,
                new InquiryTitle(TITLE),
                new InquiryContent(CONTENT),
                CREATED_AT,
                UPDATED_AT,
                answeredAt
        );
    }

    private String body(String contactEmail, String title, String content) {
        return """
                {"contactEmail": "%s", "title": "%s", "content": "%s"}
                """.formatted(contactEmail, title, content);
    }
}
