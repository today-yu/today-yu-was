package com.todayyu.persistence.notice.adapter;

import com.todayyu.domain.notice.entity.Notice;
import com.todayyu.domain.notice.vo.NoticeContent;
import com.todayyu.domain.notice.vo.NoticeId;
import com.todayyu.domain.notice.vo.NoticeTitle;
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
