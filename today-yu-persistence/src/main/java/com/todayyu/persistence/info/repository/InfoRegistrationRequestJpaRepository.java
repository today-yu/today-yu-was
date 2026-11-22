package com.todayyu.persistence.info.repository;

import com.todayyu.persistence.info.entity.InfoRegistrationRequestJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InfoRegistrationRequestJpaRepository
        extends JpaRepository<InfoRegistrationRequestJpaEntity, Long>, CustomInfoRegistrationRequestRepository {
    List<InfoRegistrationRequestJpaEntity> findAllByRequesterId(Long requesterId);
}
