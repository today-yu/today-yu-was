package com.todayyu.apis.user.scrap.controller;

import com.todayyu.apis.common.exception.GlobalExceptionHandler;
import com.todayyu.apis.common.security.principal.CustomUserDetails;
import com.todayyu.application.info.error.BusinessInfoErrorType;
import com.todayyu.application.scrap.dto.command.ScrapCommand;
import com.todayyu.application.scrap.usecase.ScrapUsecase;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ScrapControllerTest {
    private static final String SCRAP_URL = "/api/v1/infos/{infoId}/scrap";
    private static final Long MEMBER_ID = 1L;
    private static final Long INFO_ID = 5L;

    @Mock
    private ScrapUsecase scrapUsecase;

    @InjectMocks
    private ScrapController scrapController;

    @Captor
    private ArgumentCaptor<ScrapCommand> commandCaptor;

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
}
