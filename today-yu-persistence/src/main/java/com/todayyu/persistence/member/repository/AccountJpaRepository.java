package com.todayyu.persistence.member.repository;

import com.todayyu.persistence.member.entity.AccountJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AccountJpaRepository extends JpaRepository<AccountJpaEntity, Long> {
    List<AccountJpaEntity> findByMemberId(Long memberId);
}
