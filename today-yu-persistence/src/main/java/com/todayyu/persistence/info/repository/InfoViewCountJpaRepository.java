package com.todayyu.persistence.info.repository;

import com.todayyu.persistence.info.entity.InfoViewCountJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InfoViewCountJpaRepository extends JpaRepository<InfoViewCountJpaEntity, Long> {
}
