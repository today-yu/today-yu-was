package com.todayyu.persistence.info.dto;

import com.todayyu.persistence.info.entity.InfoUpdateRequestJpaEntity;

public record InfoUpdateRequestProjection(
        InfoUpdateRequestJpaEntity request,
        String requesterNickname
) {
}
