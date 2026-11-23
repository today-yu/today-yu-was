package com.todayyu.persistence.scrap.repository;

import com.todayyu.persistence.scrap.entity.ScrapJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ScrapJpaRepository extends JpaRepository<ScrapJpaEntity, Long>, CustomScrapRepository {
    boolean existsByInfoIdAndMemberId(Long infoId, Long memberId);
    void deleteByInfoIdAndMemberId(Long infoId, Long memberId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM ScrapJpaEntity s WHERE s.memberId = :memberId")
    void deleteAllByMemberIdInBatch(@Param(value = "memberId") Long memberId);
}
