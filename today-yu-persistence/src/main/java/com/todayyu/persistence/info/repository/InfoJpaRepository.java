package com.todayyu.persistence.info.repository;

import com.todayyu.persistence.info.entity.InfoJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InfoJpaRepository extends JpaRepository<InfoJpaEntity, Long>, CustomInfoRepository {
}
