package com.todayyu.apis.user.info.dto.response;

import com.todayyu.application.info.dto.result.SubmitInfoUpdateRequestResult;

public record SubmitInfoUpdateRequestResponse(
        Long infoUpdateRequestId
) {
    public static SubmitInfoUpdateRequestResponse from(SubmitInfoUpdateRequestResult result) {
        return new SubmitInfoUpdateRequestResponse(result.id().getValue());
    }
}
