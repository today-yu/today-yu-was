package com.todayyu.persistence.member.repository;

import com.todayyu.domain.member.vo.AuthProvider;
import com.todayyu.persistence.member.entity.AccountJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AccountJpaRepository extends JpaRepository<AccountJpaEntity, Long> {
    List<AccountJpaEntity> findByMemberId(Long memberId);
    Optional<AccountJpaEntity> findByAuthProviderAndAuthProviderUserId(AuthProvider authProvider, String authProviderUserId);
}
