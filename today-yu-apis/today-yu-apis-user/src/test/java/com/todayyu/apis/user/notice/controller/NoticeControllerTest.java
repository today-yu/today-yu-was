package com.todayyu.apis.user.notice.controller;

import com.todayyu.apis.common.exception.GlobalExceptionHandler;
import com.todayyu.application.notice.dto.query.GetNoticeDetailQuery;
import com.todayyu.application.notice.dto.result.NoticeDetailResult;
import com.todayyu.application.notice.error.BusinessNoticeErrorType;
import com.todayyu.application.notice.usecase.GetNoticeDetailUsecase;
import com.todayyu.common.exception.CustomException;
import com.todayyu.domain.notice.vo.NoticeContent;
import com.todayyu.domain.notice.vo.NoticeId;
import com.todayyu.domain.notice.vo.NoticeTitle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class NoticeControllerTest {
    private static final String NOTICE_DETAIL_URL = "/api/v1/notices/{noticeId}";
    private static final Long NOTICE_ID = 1L;
    private static final String TITLE = "서비스 점검 안내";
    private static final String CONTENT = "10월 20일 오전 2시부터 4시까지 서비스 점검이 진행됩니다.";
    private static final LocalDateTime PUBLISHED_AT = LocalDateTime.of(2026, 10, 18, 9, 0, 30);
    private static final LocalDateTime UPDATED_AT = LocalDateTime.of(2026, 10, 19, 18, 30, 30);

    @Mock
    private GetNoticeDetailUsecase getNoticeDetailUsecase;

    @InjectMocks
    private NoticeController noticeController;

    @Captor
    private ArgumentCaptor<GetNoticeDetailQuery> getNoticeDetailQueryCaptor;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(noticeController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("공지 상세 조회 요청 시 공지 ID로 조회를 요청하고, 공지 상세 내용을 원시값으로 변환해 반환한다")
    void getNoticeDetail() throws Exception {
        // given
        given(getNoticeDetailUsecase.getNoticeDetail(any())).willReturn(new NoticeDetailResult(
                new NoticeId(NOTICE_ID),
                new NoticeTitle(TITLE),
                new NoticeContent(CONTENT),
                PUBLISHED_AT,
                UPDATED_AT
        ));

        // when
        mockMvc.perform(get(NOTICE_DETAIL_URL, NOTICE_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("공지 상세 조회에 성공하였습니다."))
                .andExpect(jsonPath("$.data.id").value(NOTICE_ID))
                .andExpect(jsonPath("$.data.title").value(TITLE))
                .andExpect(jsonPath("$.data.content").value(CONTENT))
                .andExpect(jsonPath("$.data.publishedAt").value(PUBLISHED_AT.toString()))
                .andExpect(jsonPath("$.data.updatedAt").value(UPDATED_AT.toString()));

        // then
        then(getNoticeDetailUsecase).should().getNoticeDetail(getNoticeDetailQueryCaptor.capture());
        assertEquals(new NoticeId(NOTICE_ID), getNoticeDetailQueryCaptor.getValue().id());
    }

    @Test
    @DisplayName("존재하지 않는 공지를 조회하면 공지 없음 에러를 응답한다")
    void getNoticeDetailWithNotExistingNotice() throws Exception {
        // given
        given(getNoticeDetailUsecase.getNoticeDetail(any()))
                .willThrow(new CustomException(BusinessNoticeErrorType.NOT_FOUND));

        // when & then
        mockMvc.perform(get(NOTICE_DETAIL_URL, NOTICE_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value(BusinessNoticeErrorType.NOT_FOUND.getErrorCode()))
                .andExpect(jsonPath("$.message").value(BusinessNoticeErrorType.NOT_FOUND.getMessage()));
    }

    @Test
    @DisplayName("공지 ID가 숫자가 아니면 잘못된 요청 에러를 응답하고 조회를 하지 않는다")
    void getNoticeDetailWithInvalidNoticeId() throws Exception {
        // given & when
        mockMvc.perform(get(NOTICE_DETAIL_URL, "invalid-id"))
                .andExpect(status().isBadRequest());

        // then
        then(getNoticeDetailUsecase).should(never()).getNoticeDetail(any());
    }
}
