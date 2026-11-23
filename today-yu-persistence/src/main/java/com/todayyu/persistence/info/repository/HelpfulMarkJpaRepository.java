package com.todayyu.persistence.info.repository;

import com.todayyu.persistence.info.entity.HelpfulMarkJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface HelpfulMarkJpaRepository extends JpaRepository<HelpfulMarkJpaEntity, Long>, CustomHelpfulMarkRepository {
    long countByInfoId(Long infoId);
    boolean existsByInfoIdAndMemberId(Long infoId, Long memberId);
    void deleteByInfoIdAndMemberId(Long infoId, Long memberId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM HelpfulMarkJpaEntity h WHERE h.memberId = :memberId")
    void deleteAllByMemberIdInBatch(@Param(value = "memberId") Long memberId);
}
