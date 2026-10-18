package com.todayyu.persistence.notice.repository;

import com.todayyu.persistence.notice.entity.NoticeJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NoticeJpaRepository extends JpaRepository<NoticeJpaEntity, Long> {
}
