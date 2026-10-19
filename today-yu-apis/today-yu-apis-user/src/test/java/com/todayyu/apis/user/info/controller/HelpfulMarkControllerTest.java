package com.todayyu.apis.user.info.controller;

import com.todayyu.apis.common.exception.GlobalExceptionHandler;
import com.todayyu.apis.common.security.principal.CustomUserDetails;
import com.todayyu.application.info.dto.command.MarkHelpfulCommand;
import com.todayyu.application.info.dto.command.UnmarkHelpfulCommand;
import com.todayyu.application.info.error.BusinessInfoErrorType;
import com.todayyu.application.info.usecase.MarkHelpfulUsecase;
import com.todayyu.application.info.usecase.UnmarkHelpfulUsecase;
import com.todayyu.common.exception.CustomException;
import com.todayyu.domain.info.vo.InfoId;
import com.todayyu.domain.member.vo.MemberId;
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

import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class HelpfulMarkControllerTest {
    private static final String HELPFUL_MARK_URL = "/api/v1/infos/{infoId}/helpful-mark";
    private static final Long MEMBER_ID = 1L;
    private static final Long INFO_ID = 5L;

    @Mock
    private MarkHelpfulUsecase markHelpfulUsecase;

    @Mock
    private UnmarkHelpfulUsecase unmarkHelpfulUsecase;

    @InjectMocks
    private HelpfulMarkController helpfulMarkController;

    @Captor
    private ArgumentCaptor<MarkHelpfulCommand> commandCaptor;

    @Captor
    private ArgumentCaptor<UnmarkHelpfulCommand> unmarkCommandCaptor;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(helpfulMarkController)
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
    @DisplayName("'도움이 되는 정보에요' 표시 요청 시 경로의 정보 ID와 인증된 회원 ID로 표시를 요청하고, 성공 메시지를 반환한다")
    void markHelpful() throws Exception {
        // when
        mockMvc.perform(put(HELPFUL_MARK_URL, INFO_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("'도움이 되는 정보에요' 표시에 성공하였습니다."))
                .andExpect(jsonPath("$.data").value(nullValue()));

        // then
        then(markHelpfulUsecase).should().markHelpful(commandCaptor.capture());
        MarkHelpfulCommand command = commandCaptor.getValue();
        assertEquals(new InfoId(INFO_ID), command.infoId());
        assertEquals(new MemberId(MEMBER_ID), command.memberId());
    }

    @Test
    @DisplayName("'도움이 되는 정보에요'를 표시할 정보가 존재하지 않으면 정보 없음 에러를 응답한다")
    void markHelpfulWithNonExistentInfo() throws Exception {
        // given
        willThrow(new CustomException(BusinessInfoErrorType.NOT_FOUND))
                .given(markHelpfulUsecase).markHelpful(any());

        // when & then
        mockMvc.perform(put(HELPFUL_MARK_URL, INFO_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value(BusinessInfoErrorType.NOT_FOUND.getErrorCode()))
                .andExpect(jsonPath("$.message").value(BusinessInfoErrorType.NOT_FOUND.getMessage()));
    }

    @Test
    @DisplayName("정보 ID가 숫자가 아니면 잘못된 요청 에러를 응답하고 '도움이 되는 정보에요'를 표시하지 않는다")
    void markHelpfulWithInvalidInfoId() throws Exception {
        // given & when
        mockMvc.perform(put(HELPFUL_MARK_URL, "invalid-id"))
                .andExpect(status().isBadRequest());

        // then
        then(markHelpfulUsecase).should(never()).markHelpful(any());
    }

    @Test
    @DisplayName("'도움이 되는 정보에요' 해제 요청 시 경로의 정보 ID와 인증된 회원 ID로 해제를 요청하고, 성공 메시지를 반환한다")
    void unmarkHelpful() throws Exception {
        // when
        mockMvc.perform(delete(HELPFUL_MARK_URL, INFO_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("'도움이 되는 정보에요' 해제에 성공하였습니다."))
                .andExpect(jsonPath("$.data").value(nullValue()));

        // then
        then(unmarkHelpfulUsecase).should().unmarkHelpful(unmarkCommandCaptor.capture());
        UnmarkHelpfulCommand command = unmarkCommandCaptor.getValue();
        assertEquals(new InfoId(INFO_ID), command.infoId());
        assertEquals(new MemberId(MEMBER_ID), command.memberId());
    }

    @Test
    @DisplayName("'도움이 되는 정보에요'를 해제할 정보가 존재하지 않으면 정보 없음 에러를 응답한다")
    void unmarkHelpfulWithNonExistentInfo() throws Exception {
        // given
        willThrow(new CustomException(BusinessInfoErrorType.NOT_FOUND))
                .given(unmarkHelpfulUsecase).unmarkHelpful(any());

        // when & then
        mockMvc.perform(delete(HELPFUL_MARK_URL, INFO_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value(BusinessInfoErrorType.NOT_FOUND.getErrorCode()))
                .andExpect(jsonPath("$.message").value(BusinessInfoErrorType.NOT_FOUND.getMessage()));
    }

    @Test
    @DisplayName("정보 ID가 숫자가 아니면 잘못된 요청 에러를 응답하고 '도움이 되는 정보에요'를 해제하지 않는다")
    void unmarkHelpfulWithInvalidInfoId() throws Exception {
        // given & when
        mockMvc.perform(delete(HELPFUL_MARK_URL, "invalid-id"))
                .andExpect(status().isBadRequest());

        // then
        then(unmarkHelpfulUsecase).should(never()).unmarkHelpful(any());
    }
}
