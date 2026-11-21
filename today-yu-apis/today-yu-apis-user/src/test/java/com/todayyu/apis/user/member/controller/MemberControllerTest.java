package com.todayyu.apis.user.member.controller;

import com.todayyu.apis.common.exception.GlobalExceptionHandler;
import com.todayyu.apis.common.security.principal.CustomUserDetails;
import com.todayyu.application.member.dto.command.ChangePasswordCommand;
import com.todayyu.application.member.dto.command.WithdrawMemberCommand;
import com.todayyu.application.member.dto.query.GetMyInfoQuery;
import com.todayyu.application.member.dto.result.MyInfoResult;
import com.todayyu.application.member.error.BusinessMemberErrorType;
import com.todayyu.application.member.usecase.ChangePasswordUsecase;
import com.todayyu.application.member.usecase.GetMyInfoUsecase;
import com.todayyu.application.member.usecase.WithdrawMemberUsecase;
import com.todayyu.common.exception.CustomException;
import com.todayyu.domain.common.vo.Email;
import com.todayyu.domain.member.vo.MemberId;
import com.todayyu.domain.member.vo.Nickname;
import com.todayyu.domain.member.vo.Password;
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
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class MemberControllerTest {
    private static final String MY_INFO_URL = "/api/v1/members/me";
    private static final String CHANGE_PASSWORD_URL = "/api/v1/members/password";
    private static final String WITHDRAW_URL = "/api/v1/members/me/withdrawal";
    private static final Long MEMBER_ID = 1L;
    private static final String NICKNAME = "명랑한 영대인 1";
    private static final String EMAIL = "member@example.com";
    private static final String CURRENT_PASSWORD = "current1234";
    private static final String NEW_PASSWORD = "new-password1234";
    private static final String WITHDRAW_PASSWORD = "password1234";

    @Mock
    private GetMyInfoUsecase getMyInfoUsecase;

    @Mock
    private ChangePasswordUsecase changePasswordUsecase;

    @Mock
    private WithdrawMemberUsecase withdrawMemberUsecase;

    @InjectMocks
    private MemberController memberController;

    @Captor
    private ArgumentCaptor<GetMyInfoQuery> queryCaptor;

    @Captor
    private ArgumentCaptor<ChangePasswordCommand> changePasswordCommandCaptor;

    @Captor
    private ArgumentCaptor<WithdrawMemberCommand> withdrawMemberCommandCaptor;

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

    @Test
    @DisplayName("비밀번호 변경 시 인증된 회원 ID와 현재·새 비밀번호로 변경을 요청하고 성공 응답을 반환한다")
    void changePassword() throws Exception {
        // given & when
        mockMvc.perform(patch(CHANGE_PASSWORD_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"currentPassword": "%s", "newPassword": "%s"}
                                """.formatted(CURRENT_PASSWORD, NEW_PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("비밀번호 변경에 성공하였습니다."));

        // then
        then(changePasswordUsecase).should().change(changePasswordCommandCaptor.capture());
        ChangePasswordCommand command = changePasswordCommandCaptor.getValue();
        assertEquals(new MemberId(MEMBER_ID), command.memberId());
        assertEquals(new Password(CURRENT_PASSWORD), command.currentPassword());
        assertEquals(new Password(NEW_PASSWORD), command.newPassword());
    }

    @Test
    @DisplayName("현재 비밀번호가 없으면 유효성 검증 에러를 응답하고 비밀번호를 변경하지 않는다")
    void changePasswordWithoutCurrentPassword() throws Exception {
        // given & when
        mockMvc.perform(patch(CHANGE_PASSWORD_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"newPassword": "%s"}
                                """.formatted(NEW_PASSWORD)))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.errorCode").value("COMMON-0002"))
                .andExpect(jsonPath("$.errors.currentPassword").value("비밀번호는 비어있을 수 없습니다."));

        // then
        then(changePasswordUsecase).should(never()).change(any());
    }

    @Test
    @DisplayName("새 비밀번호의 길이가 비밀번호 정책과 다르면 유효성 검증 에러를 응답하고 비밀번호를 변경하지 않는다")
    void changePasswordWithInvalidNewPasswordLength() throws Exception {
        // given & when
        mockMvc.perform(patch(CHANGE_PASSWORD_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"currentPassword": "%s", "newPassword": "short"}
                                """.formatted(CURRENT_PASSWORD)))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.errorCode").value("COMMON-0002"))
                .andExpect(jsonPath("$.errors.newPassword").exists());

        // then
        then(changePasswordUsecase).should(never()).change(any());
    }

    @Test
    @DisplayName("현재 비밀번호가 일치하지 않으면 현재 비밀번호 불일치 에러를 응답한다")
    void changePasswordWithMismatchedCurrentPassword() throws Exception {
        // given
        willThrow(new CustomException(BusinessMemberErrorType.CURRENT_PASSWORD_MISMATCH))
                .given(changePasswordUsecase).change(any());

        // when & then
        mockMvc.perform(patch(CHANGE_PASSWORD_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"currentPassword": "%s", "newPassword": "%s"}
                                """.formatted(CURRENT_PASSWORD, NEW_PASSWORD)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value(BusinessMemberErrorType.CURRENT_PASSWORD_MISMATCH.getErrorCode()))
                .andExpect(jsonPath("$.message").value(BusinessMemberErrorType.CURRENT_PASSWORD_MISMATCH.getMessage()));
    }

    @Test
    @DisplayName("회원 탈퇴 시 비밀번호를 입력하면 인증된 회원 ID와 비밀번호로 탈퇴를 요청하고 성공 응답을 반환한다")
    void withdraw() throws Exception {
        // given & when
        mockMvc.perform(post(WITHDRAW_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"password": "%s"}
                                """.formatted(WITHDRAW_PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("회원 탈퇴에 성공하였습니다."));

        // then
        then(withdrawMemberUsecase).should().withdraw(withdrawMemberCommandCaptor.capture());
        WithdrawMemberCommand command = withdrawMemberCommandCaptor.getValue();
        assertEquals(new MemberId(MEMBER_ID), command.memberId());
        assertEquals(new Password(WITHDRAW_PASSWORD), command.password());
    }

    @Test
    @DisplayName("회원 탈퇴 시 요청 본문이 없으면(소셜 회원) 비밀번호 없이 탈퇴를 요청하고 성공 응답을 반환한다")
    void withdrawWithoutRequestBody() throws Exception {
        // given & when
        mockMvc.perform(post(WITHDRAW_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("회원 탈퇴에 성공하였습니다."));

        // then
        then(withdrawMemberUsecase).should().withdraw(withdrawMemberCommandCaptor.capture());
        WithdrawMemberCommand command = withdrawMemberCommandCaptor.getValue();
        assertEquals(new MemberId(MEMBER_ID), command.memberId());
        assertNull(command.password());
    }

    @Test
    @DisplayName("회원 탈퇴 시 비밀번호 필드가 없으면 비밀번호 없이 탈퇴를 요청하고 성공 응답을 반환한다")
    void withdrawWithoutPassword() throws Exception {
        // given & when
        mockMvc.perform(post(WITHDRAW_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("회원 탈퇴에 성공하였습니다."));

        // then
        then(withdrawMemberUsecase).should().withdraw(withdrawMemberCommandCaptor.capture());
        assertNull(withdrawMemberCommandCaptor.getValue().password());
    }

    @Test
    @DisplayName("회원 탈퇴 시 비밀번호의 길이가 비밀번호 정책과 다르면 유효성 검증 에러를 응답하고 탈퇴를 요청하지 않는다")
    void withdrawWithInvalidPasswordLength() throws Exception {
        // given & when
        mockMvc.perform(post(WITHDRAW_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"password": "short"}
                                """))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.errorCode").value("COMMON-0002"))
                .andExpect(jsonPath("$.errors.password").exists());

        // then
        then(withdrawMemberUsecase).should(never()).withdraw(any());
    }

    @Test
    @DisplayName("로컬 회원이 비밀번호 없이 탈퇴를 요청하면 비밀번호 필수 에러를 응답한다")
    void withdrawLocalMemberWithoutPassword() throws Exception {
        // given
        willThrow(new CustomException(BusinessMemberErrorType.PASSWORD_REQUIRED))
                .given(withdrawMemberUsecase).withdraw(any());

        // when & then
        mockMvc.perform(post(WITHDRAW_URL))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value(BusinessMemberErrorType.PASSWORD_REQUIRED.getErrorCode()))
                .andExpect(jsonPath("$.message").value(BusinessMemberErrorType.PASSWORD_REQUIRED.getMessage()));
    }

    @Test
    @DisplayName("회원 탈퇴 시 비밀번호가 일치하지 않으면 비밀번호 불일치 에러를 응답한다")
    void withdrawWithMismatchedPassword() throws Exception {
        // given
        willThrow(new CustomException(BusinessMemberErrorType.CURRENT_PASSWORD_MISMATCH))
                .given(withdrawMemberUsecase).withdraw(any());

        // when & then
        mockMvc.perform(post(WITHDRAW_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"password": "%s"}
                                """.formatted(WITHDRAW_PASSWORD)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value(BusinessMemberErrorType.CURRENT_PASSWORD_MISMATCH.getErrorCode()))
                .andExpect(jsonPath("$.message").value(BusinessMemberErrorType.CURRENT_PASSWORD_MISMATCH.getMessage()));
    }
}
