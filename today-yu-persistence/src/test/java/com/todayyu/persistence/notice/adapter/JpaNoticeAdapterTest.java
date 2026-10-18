package com.todayyu.persistence.notice.adapter;

import com.todayyu.application.notice.port.dto.NoticeCursor;
import com.todayyu.application.notice.port.dto.NoticeSummaryView;
import com.todayyu.domain.notice.entity.Notice;
import com.todayyu.domain.notice.vo.NoticeContent;
import com.todayyu.domain.notice.vo.NoticeId;
import com.todayyu.domain.notice.vo.NoticeTitle;
import com.todayyu.persistence.notice.dto.NoticeSummaryViewProjection;
import com.todayyu.persistence.notice.entity.NoticeJpaEntity;
import com.todayyu.persistence.notice.mapper.NoticeMapper;
import com.todayyu.persistence.notice.repository.NoticeJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class JpaNoticeAdapterTest {
    private static final Long NOTICE_ID = 1L;
    private static final LocalDateTime PUBLISHED_AT = LocalDateTime.of(2026, 10, 18, 9, 0);

    @Mock
    private NoticeJpaRepository noticeRepository;

    @Mock
    private NoticeMapper noticeMapper;

    @InjectMocks
    private JpaNoticeAdapter noticeAdapter;

    @Test
    @DisplayName("공지 ID에 해당하는 공지가 존재하면 도메인 엔티티로 변환해 반환한다")
    void readById() {
        // given
        NoticeJpaEntity jpaEntity = noticeJpaEntity();
        Notice notice = notice();
        given(noticeRepository.findById(NOTICE_ID)).willReturn(Optional.of(jpaEntity));
        given(noticeMapper.toDomain(jpaEntity)).willReturn(notice);

        // when
        Optional<Notice> result = noticeAdapter.readById(new NoticeId(NOTICE_ID));

        // then
        assertEquals(Optional.of(notice), result);
    }

    @Test
    @DisplayName("공지 ID에 해당하는 공지가 존재하지 않으면 빈 Optional을 반환하고 변환하지 않는다")
    void readByIdWithNotExistingNotice() {
        // given
        given(noticeRepository.findById(NOTICE_ID)).willReturn(Optional.empty());

        // when
        Optional<Notice> result = noticeAdapter.readById(new NoticeId(NOTICE_ID));

        // then
        assertTrue(result.isEmpty());
        then(noticeMapper).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("커서가 있으면 커서의 공지 ID로 조회하고, 조회 결과를 공지 요약 목록으로 변환해 반환한다")
    void readByCursor() {
        // given
        Long cursorId = 10L;
        given(noticeRepository.findByCursor(cursorId, 3)).willReturn(List.of(
                new NoticeSummaryViewProjection(NOTICE_ID, "서비스 점검 안내", PUBLISHED_AT)
        ));

        // when
        List<NoticeSummaryView> result = noticeAdapter.readByCursor(new NoticeCursor(new NoticeId(cursorId)), 3);

        // then
        NoticeSummaryView expected = NoticeSummaryView.builder()
                .id(new NoticeId(NOTICE_ID))
                .title(new NoticeTitle("서비스 점검 안내"))
                .publishedAt(PUBLISHED_AT)
                .build();
        assertEquals(List.of(expected), result);
    }

    @Test
    @DisplayName("커서가 없으면 커서 조건 없이 처음부터 조회한다")
    void readByCursorWithoutCursor() {
        // given
        given(noticeRepository.findByCursor(null, 3)).willReturn(List.of());

        // when
        List<NoticeSummaryView> result = noticeAdapter.readByCursor(null, 3);

        // then
        assertTrue(result.isEmpty());
        then(noticeRepository).should().findByCursor(null, 3);
    }

    private NoticeJpaEntity noticeJpaEntity() {
        NoticeJpaEntity jpaEntity = NoticeJpaEntity.builder()
                .title("서비스 점검 안내")
                .content("10월 20일 오전 2시부터 4시까지 서비스 점검이 진행됩니다.")
                .publishedAt(PUBLISHED_AT)
                .updatedAt(PUBLISHED_AT)
                .build();
        ReflectionTestUtils.setField(jpaEntity, "id", NOTICE_ID);
        return jpaEntity;
    }

    private Notice notice() {
        return Notice.withId(
                new NoticeId(NOTICE_ID),
                new NoticeTitle("서비스 점검 안내"),
                new NoticeContent("10월 20일 오전 2시부터 4시까지 서비스 점검이 진행됩니다."),
                PUBLISHED_AT,
                PUBLISHED_AT
        );
    }
}
