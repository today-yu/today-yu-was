package com.todayyu.apis.user.member.controller;

import com.todayyu.apis.common.exception.GlobalExceptionHandler;
import com.todayyu.apis.common.security.principal.CustomUserDetails;
import com.todayyu.application.member.dto.query.GetMyInfoQuery;
import com.todayyu.application.member.dto.result.MyInfoResult;
import com.todayyu.application.member.error.BusinessMemberErrorType;
import com.todayyu.application.member.usecase.GetMyInfoUsecase;
import com.todayyu.common.exception.CustomException;
import com.todayyu.domain.common.vo.Email;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class MemberControllerTest {
    private static final String MY_INFO_URL = "/api/v1/members/me";
    private static final Long MEMBER_ID = 1L;
    private static final String NICKNAME = "명랑한 영대인 1";
    private static final String EMAIL = "member@example.com";

    @Mock
    private GetMyInfoUsecase getMyInfoUsecase;

    @InjectMocks
    private MemberController memberController;

    @Captor
    private ArgumentCaptor<GetMyInfoQuery> queryCaptor;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(memberController)
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
    @DisplayName("내 정보 조회 시 인증된 회원 ID로 조회를 요청하고, 회원 ID, 닉네임, 이메일을 반환한다")
    void getMyInfo() throws Exception {
        // given
        given(getMyInfoUsecase.getMyInfo(any()))
                .willReturn(new MyInfoResult(new MemberId(MEMBER_ID), new Nickname(NICKNAME), new Email(EMAIL)));

        // when
        mockMvc.perform(get(MY_INFO_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("내 정보 조회에 성공하였습니다."))
                .andExpect(jsonPath("$.data.memberId").value(MEMBER_ID))
                .andExpect(jsonPath("$.data.nickname").value(NICKNAME))
                .andExpect(jsonPath("$.data.email").value(EMAIL));

        // then
        then(getMyInfoUsecase).should().getMyInfo(queryCaptor.capture());
        assertEquals(new MemberId(MEMBER_ID), queryCaptor.getValue().memberId());
    }

    @Test
    @DisplayName("회원이 존재하지 않으면 회원 없음 에러를 응답한다")
    void getMyInfoWithNotExistingMember() throws Exception {
        // given
        given(getMyInfoUsecase.getMyInfo(any()))
                .willThrow(new CustomException(BusinessMemberErrorType.NOT_FOUND));

        // when & then
        mockMvc.perform(get(MY_INFO_URL))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value(BusinessMemberErrorType.NOT_FOUND.getErrorCode()))
                .andExpect(jsonPath("$.message").value(BusinessMemberErrorType.NOT_FOUND.getMessage()));
    }
}
