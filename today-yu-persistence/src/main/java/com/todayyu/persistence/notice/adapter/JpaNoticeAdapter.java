package com.todayyu.persistence.notice.adapter;

import com.todayyu.application.notice.dto.NoticeCursor;
import com.todayyu.application.notice.port.dto.NoticeSummaryView;
import com.todayyu.application.notice.port.read.ReadNoticePort;
import com.todayyu.domain.notice.entity.Notice;
import com.todayyu.domain.notice.vo.NoticeId;
import com.todayyu.persistence.common.PersistenceAdapter;
import com.todayyu.persistence.notice.dto.NoticeSummaryViewProjection;
import com.todayyu.persistence.notice.mapper.NoticeMapper;
import com.todayyu.persistence.notice.repository.NoticeJpaRepository;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

@PersistenceAdapter
@RequiredArgsConstructor
public class JpaNoticeAdapter implements ReadNoticePort {
    private final NoticeJpaRepository noticeRepository;
    private final NoticeMapper noticeMapper;

    /**
     * 공지 ID({@link NoticeId})를 통해 공지({@link Notice})를 조회해 반환하는 메서드
     *
     * @param id 공지 ID 객체
     * @return 공지 객체
     */
    @Override
    public Optional<Notice> readById(NoticeId id) {
        return noticeRepository.findById(id.getValue())
                .map(noticeMapper::toDomain);
    }
    
    /**
     * 커서({@link NoticeCursor}) 이후의 공지 목록을 최신 발행 순으로 조회해 공지 요약({@link NoticeSummaryView}) 목록으로 반환하는 메서드
     *
     * <p> 커서가 {@code null}이면 처음부터 조회한다. </p>
     *
     * @param cursor 마지막으로 조회한 공지의 커서
     * @param limit  최대 조회 개수
     * @return 공지 요약 목록
     */
    @Override
    public List<NoticeSummaryView> readByCursor(NoticeCursor cursor, int limit) {
        Long cursorId = (cursor == null) ? null : cursor.noticeId().getValue();
        
        List<NoticeSummaryViewProjection> projections = noticeRepository.findByCursor(cursorId, limit);
        
        return projections.stream()
                .map(NoticeSummaryViewProjection::toView)
                .toList();
    }
}
