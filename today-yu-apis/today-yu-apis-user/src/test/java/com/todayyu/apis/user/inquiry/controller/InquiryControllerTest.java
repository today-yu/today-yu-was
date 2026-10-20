package com.todayyu.apis.user.inquiry.controller;

import com.todayyu.apis.common.exception.GlobalExceptionHandler;
import com.todayyu.apis.common.security.principal.CustomUserDetails;
import com.todayyu.application.inquiry.dto.command.SubmitInquiryCommand;
import com.todayyu.application.inquiry.dto.result.SubmitInquiryResult;
import com.todayyu.application.inquiry.usecase.SubmitInquiryUsecase;
import com.todayyu.common.error.CommonErrorType;
import com.todayyu.domain.common.vo.Email;
import com.todayyu.domain.inquiry.vo.InquiryContent;
import com.todayyu.domain.inquiry.vo.InquiryId;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class InquiryControllerTest {
    private static final String SUBMIT_URL = "/api/v1/inquiries";
    private static final Long MEMBER_ID = 1L;
    private static final Long INQUIRY_ID = 10L;
    private static final String CONTACT_EMAIL = "member@example.com";
    private static final String TITLE = "문의 제목";
    private static final String CONTENT = "문의 내용";

    @Mock
    private SubmitInquiryUsecase submitInquiryUsecase;

    @InjectMocks
    private InquiryController inquiryController;

    @Captor
    private ArgumentCaptor<SubmitInquiryCommand> commandCaptor;

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

    private String body(String contactEmail, String title, String content) {
        return """
                {"contactEmail": "%s", "title": "%s", "content": "%s"}
                """.formatted(contactEmail, title, content);
    }
}
