package com.todayyu.application.notice.usecase;

import com.todayyu.application.notice.dto.query.GetNoticeDetailQuery;
import com.todayyu.application.notice.dto.result.NoticeDetailResult;
import com.todayyu.application.notice.error.BusinessNoticeErrorType;
import com.todayyu.application.notice.port.read.ReadNoticePort;
import com.todayyu.common.exception.CustomException;
import com.todayyu.domain.notice.entity.Notice;
import com.todayyu.domain.notice.vo.NoticeContent;
import com.todayyu.domain.notice.vo.NoticeId;
import com.todayyu.domain.notice.vo.NoticeTitle;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class GetNoticeDetailUsecaseTest {
    private static final NoticeId NOTICE_ID = new NoticeId(1L);
    private static final NoticeTitle TITLE = new NoticeTitle("서비스 점검 안내");
    private static final NoticeContent CONTENT = new NoticeContent("10월 20일 오전 2시부터 4시까지 서비스 점검이 진행됩니다.");
    private static final LocalDateTime PUBLISHED_AT = LocalDateTime.of(2026, 10, 18, 9, 0);
    private static final LocalDateTime UPDATED_AT = LocalDateTime.of(2026, 10, 19, 18, 30);

    @Mock
    private ReadNoticePort readNoticePort;

    @InjectMocks
    private GetNoticeDetailUsecase getNoticeDetailUsecase;

    @Test
    @DisplayName("공지가 존재하면 공지의 상세 내용을 반환한다")
    void getNoticeDetail() {
        // given
        given(readNoticePort.readById(NOTICE_ID)).willReturn(Optional.of(notice()));

        // when
        NoticeDetailResult result = getNoticeDetailUsecase.getNoticeDetail(new GetNoticeDetailQuery(NOTICE_ID));

        // then
        assertEquals(NOTICE_ID, result.id());
        assertEquals(TITLE, result.title());
        assertEquals(CONTENT, result.content());
        assertEquals(PUBLISHED_AT, result.publishedAt());
        assertEquals(UPDATED_AT, result.updatedAt());
    }

    @Test
    @DisplayName("공지가 존재하지 않으면 NOT_FOUND 예외가 발생한다")
    void getNoticeDetailWithNotExistingNotice() {
        // given
        given(readNoticePort.readById(NOTICE_ID)).willReturn(Optional.empty());

        // when
        CustomException exception = assertThrows(CustomException.class,
                () -> getNoticeDetailUsecase.getNoticeDetail(new GetNoticeDetailQuery(NOTICE_ID)));

        // then
        assertEquals(BusinessNoticeErrorType.NOT_FOUND, exception.getErrorType());
    }

    private Notice notice() {
        return Notice.withId(NOTICE_ID, TITLE, CONTENT, PUBLISHED_AT, UPDATED_AT);
    }
}
