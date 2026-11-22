package com.todayyu.persistence.info.repository;

import com.todayyu.persistence.info.entity.InfoUpdateRequestJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InfoUpdateRequestJpaRepository
        extends JpaRepository<InfoUpdateRequestJpaEntity, Long>, CustomInfoUpdateRequestRepository {
    List<InfoUpdateRequestJpaEntity> findAllByRequesterId(Long requesterId);
}
