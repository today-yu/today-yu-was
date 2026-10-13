package com.todayyu.application.info.port.dto;

import com.todayyu.domain.info.entity.InfoUpdateRequest;
import com.todayyu.domain.member.vo.Nickname;

public record InfoUpdateRequestView(
        InfoUpdateRequest request,
        Nickname requesterNickname
) {
}
