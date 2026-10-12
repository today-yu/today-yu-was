package com.todayyu.persistence.info.repository;

import com.todayyu.persistence.info.entity.HelpfulMarkJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HelpfulMarkJpaRepository extends JpaRepository<HelpfulMarkJpaEntity, Long>, CustomHelpfulMarkRepository {
    long countByInfoId(Long infoId);
    boolean existsByInfoIdAndMemberId(Long infoId, Long memberId);
}
