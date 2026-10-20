package com.todayyu.apis.user.member.dto.response;

import com.todayyu.application.member.dto.result.MyInfoResult;

public record MyInfoResponse(
        Long memberId,
        String nickname,
        String email
) {
    public static MyInfoResponse from(MyInfoResult result) {
        return new MyInfoResponse(
                result.memberId().getValue(),
                result.nickname().getValue(),
                result.email().getValue()
        );
    }
}
