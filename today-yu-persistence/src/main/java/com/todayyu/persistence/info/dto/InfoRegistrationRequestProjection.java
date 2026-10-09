package com.todayyu.persistence.info.dto;

import com.todayyu.persistence.info.entity.InfoRegistrationRequestJpaEntity;

public record InfoRegistrationRequestProjection (
        InfoRegistrationRequestJpaEntity request,
        String requesterNickname
) {
}
