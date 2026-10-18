package com.todayyu.apis.user.notice.controller;

import com.todayyu.apis.common.exception.GlobalExceptionHandler;
import com.todayyu.application.common.dto.CursorResult;
import com.todayyu.application.notice.dto.query.GetNoticeDetailQuery;
import com.todayyu.application.notice.dto.query.GetNoticesQuery;
import com.todayyu.application.notice.dto.result.NoticeDetailResult;
import com.todayyu.application.notice.dto.result.NoticeSummaryResult;
import com.todayyu.application.notice.error.BusinessNoticeErrorType;
import com.todayyu.application.notice.port.dto.NoticeCursor;
import com.todayyu.application.notice.usecase.GetNoticeDetailUsecase;
import com.todayyu.application.notice.usecase.GetNoticesUsecase;
import com.todayyu.common.error.CommonErrorType;
import com.todayyu.common.exception.CustomException;
import com.todayyu.domain.notice.vo.NoticeContent;
import com.todayyu.domain.notice.vo.NoticeId;
import com.todayyu.domain.notice.vo.NoticeTitle;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class NoticeControllerTest {
    private static final String NOTICE_LIST_URL = "/api/v1/notices";
    private static final String NOTICE_DETAIL_URL = "/api/v1/notices/{noticeId}";
    private static final Long NOTICE_ID = 1L;
    private static final String TITLE = "서비스 점검 안내";
    private static final String CONTENT = "10월 20일 오전 2시부터 4시까지 서비스 점검이 진행됩니다.";
    private static final LocalDateTime PUBLISHED_AT = LocalDateTime.of(2026, 10, 18, 9, 0, 30);
    private static final LocalDateTime UPDATED_AT = LocalDateTime.of(2026, 10, 19, 18, 30, 30);
    private static final Long SECOND_NOTICE_ID = 2L;
    private static final String SECOND_TITLE = "개인정보 처리방침 변경 안내";
    private static final LocalDateTime SECOND_PUBLISHED_AT = LocalDateTime.of(2026, 10, 17, 9, 0, 30);
    private static final Long CURSOR_NOTICE_ID = 3L;

    @Mock
    private GetNoticeDetailUsecase getNoticeDetailUsecase;

    @Mock
    private GetNoticesUsecase getNoticesUsecase;

    @InjectMocks
    private NoticeController noticeController;

    @Captor
    private ArgumentCaptor<GetNoticeDetailQuery> getNoticeDetailQueryCaptor;

    @Captor
    private ArgumentCaptor<GetNoticesQuery> getNoticesQueryCaptor;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(noticeController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("공지 목록 조회 요청 시 커서·조회 크기로 조회를 요청하고, 목록과 다음 커서를 원시값으로 변환해 반환한다")
    void getNotices() throws Exception {
        // given
        given(getNoticesUsecase.getNotices(any())).willReturn(new CursorResult<>(
                List.of(
                        new NoticeSummaryResult(new NoticeId(SECOND_NOTICE_ID), new NoticeTitle(SECOND_TITLE), SECOND_PUBLISHED_AT),
                        new NoticeSummaryResult(new NoticeId(NOTICE_ID), new NoticeTitle(TITLE), PUBLISHED_AT)
                ),
                true,
                new NoticeCursor(new NoticeId(NOTICE_ID))
        ));

        // when
        mockMvc.perform(get(NOTICE_LIST_URL)
                        .param("cursorId", String.valueOf(CURSOR_NOTICE_ID))
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("공지 조회에 성공하였습니다."))
                .andExpect(jsonPath("$.data.contents.length()").value(2))
                .andExpect(jsonPath("$.data.contents[0].id").value(SECOND_NOTICE_ID))
                .andExpect(jsonPath("$.data.contents[0].title").value(SECOND_TITLE))
                .andExpect(jsonPath("$.data.contents[0].publishedAt").value(SECOND_PUBLISHED_AT.toString()))
                .andExpect(jsonPath("$.data.contents[1].id").value(NOTICE_ID))
                .andExpect(jsonPath("$.data.contents[1].title").value(TITLE))
                .andExpect(jsonPath("$.data.contents[1].publishedAt").value(PUBLISHED_AT.toString()))
                .andExpect(jsonPath("$.data.hasNext").value(true))
                .andExpect(jsonPath("$.data.nextCursor.id").value(NOTICE_ID));

        // then
        then(getNoticesUsecase).should().getNotices(getNoticesQueryCaptor.capture());
        GetNoticesQuery query = getNoticesQueryCaptor.getValue();
        assertEquals(new NoticeCursor(new NoticeId(CURSOR_NOTICE_ID)), query.cursor());
        assertEquals(2, query.size());
    }

    @Test
    @DisplayName("첫 요청(커서·조회 크기 없음)이면 커서 없이 기본 조회 크기로 조회를 요청하고, 다음 데이터가 없으면 다음 커서는 null이다")
    void getNoticesWithFirstRequest() throws Exception {
        // given
        given(getNoticesUsecase.getNotices(any())).willReturn(new CursorResult<>(List.of(), false, null));

        // when
        mockMvc.perform(get(NOTICE_LIST_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.contents").isArray())
                .andExpect(jsonPath("$.data.contents.length()").value(0))
                .andExpect(jsonPath("$.data.hasNext").value(false))
                .andExpect(jsonPath("$.data.nextCursor").value(nullValue()));

        // then
        then(getNoticesUsecase).should().getNotices(getNoticesQueryCaptor.capture());
        GetNoticesQuery query = getNoticesQueryCaptor.getValue();
        assertNull(query.cursor());
        assertEquals(GetNoticesQuery.DEFAULT_SIZE, query.size());
    }

    @ParameterizedTest
    @ValueSource(ints = {GetNoticesQuery.MIN_SIZE - 1, GetNoticesQuery.MAX_SIZE + 1})
    @DisplayName("조회 크기가 허용 범위를 벗어나면 유효성 검증 에러를 응답하고 조회를 하지 않는다")
    void getNoticesWithOutOfRangeSize(int size) throws Exception {
        // given & when
        mockMvc.perform(get(NOTICE_LIST_URL)
                        .param("size", String.valueOf(size)))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.errorCode").value(CommonErrorType.VALIDATION_ERROR.getErrorCode()))
                .andExpect(jsonPath("$.errors.size").exists());

        // then
        then(getNoticesUsecase).should(never()).getNotices(any());
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
