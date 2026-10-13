package com.todayyu.application.info.port.dto;

import com.todayyu.domain.info.entity.InfoRegistrationRequest;
import com.todayyu.domain.member.vo.Nickname;

public record InfoRegistrationRequestView(
        InfoRegistrationRequest request,
        Nickname requesterNickname
) {
}
