package com.todayyu.persistence.scrap.repository;

import com.todayyu.persistence.scrap.entity.ScrapJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScrapJpaRepository extends JpaRepository<ScrapJpaEntity, Long> {
    boolean existsByInfoIdAndMemberId(Long infoId, Long memberId);
    void deleteByInfoIdAndMemberId(Long infoId, Long memberId);
}
