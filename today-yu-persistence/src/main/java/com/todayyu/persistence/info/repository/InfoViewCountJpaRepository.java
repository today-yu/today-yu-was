package com.todayyu.persistence.info.repository;

import com.todayyu.persistence.info.entity.InfoViewCountJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InfoViewCountJpaRepository extends JpaRepository<InfoViewCountJpaEntity, Long> {
    Optional<InfoViewCountJpaEntity> findByInfoId(Long infoId);
}
