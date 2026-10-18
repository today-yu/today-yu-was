package com.todayyu.application.notice.port.read;

import com.todayyu.domain.notice.entity.Notice;
import com.todayyu.domain.notice.vo.NoticeId;

import java.util.Optional;

public interface ReadNoticePort {
    Optional<Notice> readById(NoticeId id);
}
