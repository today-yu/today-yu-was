package com.todayyu.persistence.notice.adapter;

import com.todayyu.application.notice.port.ReadNoticePort;
import com.todayyu.domain.notice.entity.Notice;
import com.todayyu.domain.notice.vo.NoticeId;
import com.todayyu.persistence.common.PersistenceAdapter;
import com.todayyu.persistence.notice.mapper.NoticeMapper;
import com.todayyu.persistence.notice.repository.NoticeJpaRepository;
import lombok.RequiredArgsConstructor;

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
}
