package com.todayyu.application.member.port;

import com.todayyu.domain.member.vo.NicknameModifier;

public interface NicknamePort {
    long issueNext(NicknameModifier modifier);
}
