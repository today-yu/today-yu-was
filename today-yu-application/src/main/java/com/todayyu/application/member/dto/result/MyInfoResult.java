package com.todayyu.application.member.dto.result;

import com.todayyu.domain.common.vo.Email;
import com.todayyu.domain.member.entity.Member;
import com.todayyu.domain.member.vo.MemberId;
import com.todayyu.domain.member.vo.Nickname;

public record MyInfoResult(
        MemberId memberId,
        Nickname nickname,
        Email email
) {
    public static MyInfoResult from(Member member) {
        return new MyInfoResult(member.getId(), member.getNickname(), member.getEmail());
    }
}
